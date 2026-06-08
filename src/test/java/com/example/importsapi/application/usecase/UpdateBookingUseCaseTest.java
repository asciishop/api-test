package com.example.importsapi.application.usecase;

import com.example.importsapi.application.service.BookingService;
import com.example.importsapi.domain.exception.BookingNotFoundException;
import com.example.importsapi.domain.exception.InvalidBookingStateException;
import com.example.importsapi.domain.model.BookingItem;
import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.model.Supplier;
import com.example.importsapi.domain.model.enums.BookingStatus;
import com.example.importsapi.domain.model.enums.FreightMode;
import com.example.importsapi.domain.model.enums.IncotermCode;
import com.example.importsapi.domain.port.in.command.UpdateBookingCommand;
import com.example.importsapi.domain.port.out.BookingRepositoryPort;
import com.example.importsapi.domain.port.out.SupplierRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateBookingUseCase")
class UpdateBookingUseCaseTest {

    private static final String OWNER_TAX_ID = "12-3456789-0";
    private static final String OTHER_TAX_ID  = "99-9999999-9";

    @Mock private BookingRepositoryPort bookingRepository;
    @Mock private SupplierRepositoryPort supplierRepository;
    @InjectMocks private BookingService bookingService;

    private BookingRequest draftBooking;
    private BookingRequest confirmedBooking;

    @BeforeEach
    void setUp() {
        Supplier supplier = Supplier.builder()
                .id(1L).name("Acme Corp").taxId(OWNER_TAX_ID)
                .country("USA").contactEmail("contact@acme.com")
                .build();

        draftBooking = BookingRequest.builder()
                .id(1L).bookingCode("BK-001")
                .issueDate(LocalDate.of(2024, 1, 1))
                .expirationDate(LocalDate.of(2024, 12, 31))
                .currency("USD").incotermCode(IncotermCode.FOB)
                .freightMode(FreightMode.SEA)
                .fobValue(new BigDecimal("50000.00"))
                .status(BookingStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .active(true).supplier(supplier)
                .items(List.of(BookingItem.builder()
                        .id(1L).sku("SKU-001").quantity(10)
                        .unitPrice(new BigDecimal("100.00"))
                        .totalAmount(new BigDecimal("1000.00"))
                        .build()))
                .build();

        confirmedBooking = draftBooking.toBuilder().status(BookingStatus.CONFIRMED).build();
    }

    @Test
    @DisplayName("actualiza los campos permitidos y registra updatedAt")
    void updateBooking_success() {
        when(bookingRepository.findActiveById(1L)).thenReturn(Optional.of(draftBooking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateBookingCommand(
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30),
                new BigDecimal("60000.00"), "EUR"
        );
        BookingRequest result = bookingService.updateBooking(1L, command, OWNER_TAX_ID);

        assertThat(result.getCurrency()).isEqualTo("EUR");
        assertThat(result.getFobValue()).isEqualByComparingTo("60000.00");
        assertThat(result.getIssueDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("mantiene los campos existentes cuando el comando trae nulos")
    void updateBooking_keepsExistingFieldsWhenNull() {
        when(bookingRepository.findActiveById(1L)).thenReturn(Optional.of(draftBooking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookingRequest result = bookingService.updateBooking(1L, new UpdateBookingCommand(null, null, null, null), OWNER_TAX_ID);

        assertThat(result.getCurrency()).isEqualTo(draftBooking.getCurrency());
        assertThat(result.getFobValue()).isEqualByComparingTo(draftBooking.getFobValue());
        assertThat(result.getIssueDate()).isEqualTo(draftBooking.getIssueDate());
    }

    @Test
    @DisplayName("lanza excepción cuando la reserva no está en estado DRAFT")
    void updateBooking_notDraft() {
        when(bookingRepository.findActiveById(1L)).thenReturn(Optional.of(confirmedBooking));

        assertThatThrownBy(() -> bookingService.updateBooking(1L, new UpdateBookingCommand(null, null, null, null), OWNER_TAX_ID))
                .isInstanceOf(InvalidBookingStateException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    @DisplayName("lanza excepción cuando la reserva no existe")
    void updateBooking_notFound() {
        when(bookingRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBooking(99L, new UpdateBookingCommand(null, null, null, null), OWNER_TAX_ID))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    @DisplayName("lanza excepción cuando la reserva pertenece a otro proveedor")
    void updateBooking_ownershipViolation() {
        when(bookingRepository.findActiveById(1L)).thenReturn(Optional.of(draftBooking));

        assertThatThrownBy(() -> bookingService.updateBooking(1L, new UpdateBookingCommand(null, null, null, null), OTHER_TAX_ID))
                .isInstanceOf(BookingNotFoundException.class);
    }
}
