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
import com.example.importsapi.domain.port.out.BookingRepositoryPort;
import com.example.importsapi.domain.port.out.SupplierRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteBookingUseCase")
class DeleteBookingUseCaseTest {

    private static final String OWNER_TAX_ID = "12-3456789-0";
    private static final String OTHER_TAX_ID  = "99-9999999-9";

    @Mock private BookingRepositoryPort bookingRepository;
    @Mock private SupplierRepositoryPort supplierRepository;
    @InjectMocks private BookingService bookingService;

    private BookingRequest draftBooking;
    private BookingRequest confirmedBooking;
    private BookingRequest cancelledBooking;

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
        cancelledBooking = draftBooking.toBuilder().status(BookingStatus.CANCELLED).build();
    }

    @Test
    @DisplayName("soft-delete de una reserva en estado DRAFT — active=false y updatedAt registrado")
    void deleteDraft() {
        when(bookingRepository.findActiveById(1L)).thenReturn(Optional.of(draftBooking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        bookingService.deleteBooking(1L, OWNER_TAX_ID);

        ArgumentCaptor<BookingRequest> captor = ArgumentCaptor.forClass(BookingRequest.class);
        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("soft-delete de una reserva en estado CANCELADO")
    void deleteCancelled() {
        when(bookingRepository.findActiveById(1L)).thenReturn(Optional.of(cancelledBooking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        bookingService.deleteBooking(1L, OWNER_TAX_ID);

        ArgumentCaptor<BookingRequest> captor = ArgumentCaptor.forClass(BookingRequest.class);
        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    @DisplayName("lanza excepción al intentar eliminar una reserva CONFIRMADA")
    void deleteConfirmedThrows() {
        when(bookingRepository.findActiveById(1L)).thenReturn(Optional.of(confirmedBooking));

        assertThatThrownBy(() -> bookingService.deleteBooking(1L, OWNER_TAX_ID))
                .isInstanceOf(InvalidBookingStateException.class)
                .hasMessageContaining("CANCELADO");
    }

    @Test
    @DisplayName("lanza excepción cuando la reserva no existe")
    void deleteNotFound() {
        when(bookingRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deleteBooking(99L, OWNER_TAX_ID))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    @DisplayName("lanza excepción cuando la reserva pertenece a otro proveedor")
    void deleteBooking_ownershipViolation() {
        when(bookingRepository.findActiveById(1L)).thenReturn(Optional.of(draftBooking));

        assertThatThrownBy(() -> bookingService.deleteBooking(1L, OTHER_TAX_ID))
                .isInstanceOf(BookingNotFoundException.class);
    }
}
