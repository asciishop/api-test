package com.example.importsapi.infrastructure.adapter.in.rest.mapper;

import com.example.importsapi.domain.model.BookingItem;
import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.model.Supplier;
import com.example.importsapi.domain.model.enums.BookingStatus;
import com.example.importsapi.domain.model.enums.FreightMode;
import com.example.importsapi.domain.model.enums.IncotermCode;
import com.example.importsapi.domain.port.in.command.CreateBookingCommand;
import com.example.importsapi.domain.port.in.command.UpdateBookingCommand;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.request.BookingItemRequest;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.request.CreateBookingRequest;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.request.UpdateBookingRequest;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.response.BookingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookingRestMapper")
class BookingRestMapperTest {

    private BookingRestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BookingRestMapper();
    }

    @Nested
    @DisplayName("toCommand(CreateBookingRequest, taxId)")
    class ToCreateCommand {

        @Test
        @DisplayName("mapea todos los campos correctamente")
        void toCommand_mapsAllFields() {
            CreateBookingRequest request = new CreateBookingRequest(
                    "BK-001",
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31),
                    "USD", IncotermCode.FOB, FreightMode.SEA,
                    "China", "USA", new BigDecimal("50000.00"),
                    List.of(new BookingItemRequest("SKU-001", "Widget", 10, new BigDecimal("100.00")))
            );

            CreateBookingCommand command = mapper.toCommand(request, "12-3456789-0");

            assertThat(command.bookingCode()).isEqualTo("BK-001");
            assertThat(command.issueDate()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(command.expirationDate()).isEqualTo(LocalDate.of(2024, 12, 31));
            assertThat(command.currency()).isEqualTo("USD");
            assertThat(command.incotermCode()).isEqualTo(IncotermCode.FOB);
            assertThat(command.freightMode()).isEqualTo(FreightMode.SEA);
            assertThat(command.originCountry()).isEqualTo("China");
            assertThat(command.destinationCountry()).isEqualTo("USA");
            assertThat(command.fobValue()).isEqualByComparingTo("50000.00");
            assertThat(command.items()).hasSize(1);
        }

        @Test
        @DisplayName("el taxId viene del parámetro, no del request")
        void toCommand_taxIdFromParameter() {
            CreateBookingRequest request = new CreateBookingRequest(
                    "BK-001", LocalDate.now(), LocalDate.now().plusMonths(6),
                    "USD", IncotermCode.FOB, FreightMode.SEA,
                    null, null, null,
                    List.of(new BookingItemRequest("SKU-001", null, 1, new BigDecimal("10.00")))
            );

            CreateBookingCommand command = mapper.toCommand(request, "99-8888888-0");

            assertThat(command.taxId()).isEqualTo("99-8888888-0");
        }

        @Test
        @DisplayName("mapea los ítems correctamente")
        void toCommand_mapsItems() {
            CreateBookingRequest request = new CreateBookingRequest(
                    "BK-001", LocalDate.now(), LocalDate.now().plusMonths(6),
                    "USD", IncotermCode.FOB, FreightMode.SEA,
                    null, null, null,
                    List.of(
                            new BookingItemRequest("SKU-001", "Item A", 5, new BigDecimal("20.00")),
                            new BookingItemRequest("SKU-002", "Item B", 3, new BigDecimal("50.00"))
                    )
            );

            CreateBookingCommand command = mapper.toCommand(request, "12-3456789-0");

            assertThat(command.items()).hasSize(2);
            assertThat(command.items().get(0).sku()).isEqualTo("SKU-001");
            assertThat(command.items().get(1).sku()).isEqualTo("SKU-002");
        }
    }

    @Nested
    @DisplayName("toCommand(UpdateBookingRequest)")
    class ToUpdateCommand {

        @Test
        @DisplayName("mapea todos los campos correctamente")
        void toCommand_mapsAllFields() {
            UpdateBookingRequest request = new UpdateBookingRequest(
                    LocalDate.of(2024, 2, 1),
                    LocalDate.of(2024, 11, 30),
                    new BigDecimal("60000.00"),
                    "EUR"
            );

            UpdateBookingCommand command = mapper.toCommand(request);

            assertThat(command.issueDate()).isEqualTo(LocalDate.of(2024, 2, 1));
            assertThat(command.expirationDate()).isEqualTo(LocalDate.of(2024, 11, 30));
            assertThat(command.fobValue()).isEqualByComparingTo("60000.00");
            assertThat(command.currency()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("preserva nulos para PATCH parcial")
        void toCommand_preservesNulls() {
            UpdateBookingRequest request = new UpdateBookingRequest(null, null, null, null);

            UpdateBookingCommand command = mapper.toCommand(request);

            assertThat(command.issueDate()).isNull();
            assertThat(command.expirationDate()).isNull();
            assertThat(command.fobValue()).isNull();
            assertThat(command.currency()).isNull();
        }
    }

    @Nested
    @DisplayName("toResponse(BookingRequest)")
    class ToResponse {

        @Test
        @DisplayName("mapea todos los campos del dominio incluyendo updatedAt")
        void toResponse_mapsAllFields() {
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 3, 1, 15, 0);

            Supplier supplier = Supplier.builder()
                    .id(1L).name("Acme Corp").taxId("12-3456789-0")
                    .country("USA").address("123 Main St").contactEmail("contact@acme.com")
                    .build();

            BookingItem item = BookingItem.builder()
                    .id(1L).sku("SKU-001").description("Widget")
                    .quantity(10).unitPrice(new BigDecimal("100.00"))
                    .totalAmount(new BigDecimal("1000.00"))
                    .build();

            BookingRequest domain = BookingRequest.builder()
                    .id(1L).bookingCode("BK-001")
                    .issueDate(LocalDate.of(2024, 1, 1))
                    .expirationDate(LocalDate.of(2024, 12, 31))
                    .currency("USD").incotermCode(IncotermCode.FOB)
                    .freightMode(FreightMode.SEA)
                    .originCountry("China").destinationCountry("USA")
                    .fobValue(new BigDecimal("50000.00"))
                    .status(BookingStatus.DRAFT)
                    .createdAt(createdAt).updatedAt(updatedAt)
                    .active(true).supplier(supplier).items(List.of(item))
                    .build();

            BookingResponse response = mapper.toResponse(domain);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.bookingCode()).isEqualTo("BK-001");
            assertThat(response.currency()).isEqualTo("USD");
            assertThat(response.status()).isEqualTo(BookingStatus.DRAFT);
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.updatedAt()).isEqualTo(updatedAt);
            assertThat(response.supplier().taxId()).isEqualTo("12-3456789-0");
            assertThat(response.items()).hasSize(1);
            assertThat(response.items().get(0).sku()).isEqualTo("SKU-001");
        }

        @Test
        @DisplayName("updatedAt es null cuando la reserva nunca fue modificada")
        void toResponse_updatedAtNullWhenNeverModified() {
            Supplier supplier = Supplier.builder()
                    .id(1L).name("Acme").taxId("12-3456789-0")
                    .build();

            BookingRequest domain = BookingRequest.builder()
                    .id(1L).bookingCode("BK-001")
                    .issueDate(LocalDate.now()).expirationDate(LocalDate.now().plusMonths(6))
                    .currency("USD").incotermCode(IncotermCode.FOB).freightMode(FreightMode.SEA)
                    .status(BookingStatus.DRAFT).createdAt(LocalDateTime.now())
                    .active(true).supplier(supplier).items(List.of())
                    .build();

            BookingResponse response = mapper.toResponse(domain);

            assertThat(response.updatedAt()).isNull();
        }
    }
}
