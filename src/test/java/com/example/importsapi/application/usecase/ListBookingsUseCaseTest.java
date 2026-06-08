package com.example.importsapi.application.usecase;

import com.example.importsapi.application.service.BookingService;
import com.example.importsapi.domain.model.BookingItem;
import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.model.PageResult;
import com.example.importsapi.domain.model.Supplier;
import com.example.importsapi.domain.model.enums.BookingStatus;
import com.example.importsapi.domain.model.enums.FreightMode;
import com.example.importsapi.domain.model.enums.IncotermCode;
import com.example.importsapi.domain.port.in.command.BookingFilterQuery;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListBookingsUseCase")
class ListBookingsUseCaseTest {

    private static final String OWNER_TAX_ID = "12-3456789-0";

    @Mock private BookingRepositoryPort bookingRepository;
    @Mock private SupplierRepositoryPort supplierRepository;
    @InjectMocks private BookingService bookingService;

    private BookingRequest draftBooking;

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
    }

    private BookingFilterQuery query(int page, int size) {
        return new BookingFilterQuery(OWNER_TAX_ID, null, null, null, null, null, page, size);
    }

    @Test
    @DisplayName("retorna página con las reservas del proveedor autenticado")
    void listBookings_returnsPage() {
        PageResult<BookingRequest> expected = new PageResult<>(List.of(draftBooking), 0, 20, 1L, 1);
        when(bookingRepository.findByFilter(any())).thenReturn(expected);

        PageResult<BookingRequest> result = bookingService.listBookings(query(0, 20));

        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getBookingCode()).isEqualTo("BK-001");
    }

    @Test
    @DisplayName("retorna página vacía cuando no hay reservas")
    void listBookings_emptyPage() {
        when(bookingRepository.findByFilter(any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0L, 0));

        PageResult<BookingRequest> result = bookingService.listBookings(query(0, 20));

        assertThat(result.totalElements()).isEqualTo(0L);
        assertThat(result.content()).isEmpty();
    }

    @Test
    @DisplayName("delega el filtro y la paginación al repositorio sin modificarlos")
    void listBookings_delegatesQueryToRepository() {
        when(bookingRepository.findByFilter(any()))
                .thenReturn(new PageResult<>(List.of(), 0, 10, 0L, 0));

        bookingService.listBookings(query(2, 10));

        ArgumentCaptor<BookingFilterQuery> captor = ArgumentCaptor.forClass(BookingFilterQuery.class);
        verify(bookingRepository).findByFilter(captor.capture());
        assertThat(captor.getValue().taxId()).isEqualTo(OWNER_TAX_ID);
        assertThat(captor.getValue().page()).isEqualTo(2);
        assertThat(captor.getValue().size()).isEqualTo(10);
    }
}
