package com.example.importsapi.application.service;

import com.example.importsapi.domain.exception.DuplicateBookingCodeException;
import com.example.importsapi.domain.exception.InvalidBookingStateException;
import com.example.importsapi.domain.exception.SupplierNotFoundException;
import com.example.importsapi.domain.model.BookingItem;
import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.model.Supplier;
import com.example.importsapi.domain.model.enums.BookingStatus;
import com.example.importsapi.domain.model.enums.FreightMode;
import com.example.importsapi.domain.model.enums.IncotermCode;
import com.example.importsapi.domain.port.in.command.BookingItemData;
import com.example.importsapi.domain.port.in.command.CreateBookingCommand;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateBookingUseCase")
class CreateBookingUseCaseTest {

    private static final String OWNER_TAX_ID = "12-3456789-0";

    @Mock private BookingRepositoryPort bookingRepository;
    @Mock private SupplierRepositoryPort supplierRepository;
    @InjectMocks private BookingService bookingService;

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = Supplier.builder()
                .id(1L).name("Acme Corp").taxId(OWNER_TAX_ID)
                .country("USA").contactEmail("contact@acme.com")
                .build();
    }

    private CreateBookingCommand validCommand() {
        return new CreateBookingCommand(
                "BK-001",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                "USD", IncotermCode.FOB, FreightMode.SEA,
                "China", "USA", new BigDecimal("50000.00"),
                OWNER_TAX_ID,
                List.of(new BookingItemData("SKU-001", "Widget", 10, new BigDecimal("100.00")))
        );
    }

    @Test
    @DisplayName("crea la reserva con totalAmount calculado correctamente")
    void createBooking_success() {
        when(bookingRepository.existsByBookingCode("BK-001")).thenReturn(false);
        when(supplierRepository.findByTaxId(OWNER_TAX_ID)).thenReturn(Optional.of(supplier));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookingRequest result = bookingService.createBooking(validCommand());

        assertThat(result.getItems().get(0).getTotalAmount())
                .isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("el estado inicial es DRAFT y updatedAt es nulo")
    void createBooking_initialStateIsDraftWithNullUpdatedAt() {
        when(bookingRepository.existsByBookingCode(anyString())).thenReturn(false);
        when(supplierRepository.findByTaxId(anyString())).thenReturn(Optional.of(supplier));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookingRequest result = bookingService.createBooking(validCommand());

        assertThat(result.getStatus()).isEqualTo(BookingStatus.DRAFT);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("lanza excepción cuando la lista de ítems está vacía")
    void createBooking_emptyItems() {
        var command = new CreateBookingCommand(
                "BK-001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                "USD", IncotermCode.FOB, FreightMode.SEA,
                "China", "USA", new BigDecimal("50000.00"), OWNER_TAX_ID, List.of()
        );

        assertThatThrownBy(() -> bookingService.createBooking(command))
                .isInstanceOf(InvalidBookingStateException.class)
                .hasMessageContaining("al menos un ítem");
    }

    @Test
    @DisplayName("lanza excepción cuando issueDate es posterior a expirationDate")
    void createBooking_invalidDateRange() {
        var command = new CreateBookingCommand(
                "BK-001", LocalDate.of(2024, 12, 31), LocalDate.of(2024, 1, 1),
                "USD", IncotermCode.FOB, FreightMode.SEA,
                "China", "USA", new BigDecimal("50000.00"), OWNER_TAX_ID,
                List.of(new BookingItemData("SKU-001", "Widget", 10, new BigDecimal("100.00")))
        );

        assertThatThrownBy(() -> bookingService.createBooking(command))
                .isInstanceOf(InvalidBookingStateException.class)
                .hasMessageContaining("fecha de emisión");
    }

    @Test
    @DisplayName("lanza excepción cuando el bookingCode ya existe")
    void createBooking_duplicateCode() {
        when(bookingRepository.existsByBookingCode("BK-001")).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(validCommand()))
                .isInstanceOf(DuplicateBookingCodeException.class);
    }

    @Test
    @DisplayName("lanza excepción cuando el taxId del proveedor no existe")
    void createBooking_unknownSupplier() {
        when(bookingRepository.existsByBookingCode(anyString())).thenReturn(false);
        when(supplierRepository.findByTaxId(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(validCommand()))
                .isInstanceOf(SupplierNotFoundException.class);
    }
}
