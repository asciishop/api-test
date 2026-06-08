package com.example.importsapi.infrastructure.config;

import com.example.importsapi.domain.model.BookingItem;
import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.model.Supplier;
import com.example.importsapi.domain.model.User;
import com.example.importsapi.domain.model.enums.BookingStatus;
import com.example.importsapi.domain.model.enums.FreightMode;
import com.example.importsapi.domain.model.enums.IncotermCode;
import com.example.importsapi.domain.port.out.BookingRepositoryPort;
import com.example.importsapi.domain.port.out.SupplierRepositoryPort;
import com.example.importsapi.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepositoryPort userRepository;
    private final SupplierRepositoryPort supplierRepository;
    private final BookingRepositoryPort bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.hasAny()) {
            userRepository.saveAll(List.of(
                    User.builder().username("juan").password(passwordEncoder.encode("secret123")).taxId("12-3456789-0").build(),
                    User.builder().username("maria").password(passwordEncoder.encode("secret123")).taxId("98-7654321-0").build(),
                    User.builder().username("diego").password(passwordEncoder.encode("secret123")).taxId("76-5432198-0").build()
            ));
        }

        if (!supplierRepository.hasAny()) {
            supplierRepository.saveAll(List.of(
                    Supplier.builder()
                            .name("Juan Importaciones S.A.")
                            .taxId("12-3456789-0")
                            .country("China")
                            .address("Av. Principal 123, Shanghai")
                            .contactEmail("juan@importaciones.com")
                            .build(),
                    Supplier.builder()
                            .name("Maria Exports Ltd.")
                            .taxId("98-7654321-0")
                            .country("USA")
                            .address("500 Commerce St, New York")
                            .contactEmail("maria@exports.com")
                            .build(),
                    Supplier.builder()
                            .name("Diego Trading Co.")
                            .taxId("76-5432198-0")
                            .country("Germany")
                            .address("Handelsweg 22, Hamburg")
                            .contactEmail("diego@trading.com")
                            .build()
            ));
        }

        if (!bookingRepository.hasAny()) {
            Supplier juan = supplierRepository.findByTaxId("12-3456789-0").orElseThrow();
            Supplier maria = supplierRepository.findByTaxId("98-7654321-0").orElseThrow();
            Supplier diego = supplierRepository.findByTaxId("76-5432198-0").orElseThrow();

            // Juan - DRAFT
            bookingRepository.save(BookingRequest.builder()
                    .bookingCode("BK-2024-001")
                    .issueDate(LocalDate.of(2024, 1, 10))
                    .expirationDate(LocalDate.of(2024, 6, 10))
                    .currency("USD")
                    .incotermCode(IncotermCode.FOB)
                    .freightMode(FreightMode.SEA)
                    .originCountry("China")
                    .destinationCountry("Argentina")
                    .fobValue(new BigDecimal("15000.00"))
                    .status(BookingStatus.DRAFT)
                    .createdAt(LocalDateTime.of(2024, 1, 10, 9, 0))
                    .active(true)
                    .supplier(juan)
                    .items(List.of(
                            BookingItem.builder().sku("SKU-001").description("Tornillos industriales").quantity(5000).unitPrice(new BigDecimal("0.50")).totalAmount(new BigDecimal("2500.00")).build(),
                            BookingItem.builder().sku("SKU-002").description("Tuercas M8").quantity(3000).unitPrice(new BigDecimal("0.30")).totalAmount(new BigDecimal("900.00")).build()
                    ))
                    .build());

            // Juan - CONFIRMED
            bookingRepository.save(BookingRequest.builder()
                    .bookingCode("BK-2024-002")
                    .issueDate(LocalDate.of(2024, 2, 5))
                    .expirationDate(LocalDate.of(2024, 8, 5))
                    .currency("USD")
                    .incotermCode(IncotermCode.CIF)
                    .freightMode(FreightMode.AIR)
                    .originCountry("China")
                    .destinationCountry("Argentina")
                    .fobValue(new BigDecimal("32000.00"))
                    .status(BookingStatus.CONFIRMED)
                    .createdAt(LocalDateTime.of(2024, 2, 5, 10, 30))
                    .active(true)
                    .supplier(juan)
                    .items(List.of(
                            BookingItem.builder().sku("SKU-003").description("Circuitos integrados").quantity(200).unitPrice(new BigDecimal("80.00")).totalAmount(new BigDecimal("16000.00")).build(),
                            BookingItem.builder().sku("SKU-004").description("Resistencias 10k").quantity(10000).unitPrice(new BigDecimal("0.02")).totalAmount(new BigDecimal("200.00")).build()
                    ))
                    .build());

            // Maria - DRAFT
            bookingRepository.save(BookingRequest.builder()
                    .bookingCode("BK-2024-003")
                    .issueDate(LocalDate.of(2024, 3, 1))
                    .expirationDate(LocalDate.of(2024, 9, 1))
                    .currency("USD")
                    .incotermCode(IncotermCode.EXW)
                    .freightMode(FreightMode.SEA)
                    .originCountry("USA")
                    .destinationCountry("Argentina")
                    .fobValue(new BigDecimal("48000.00"))
                    .status(BookingStatus.DRAFT)
                    .createdAt(LocalDateTime.of(2024, 3, 1, 8, 0))
                    .active(true)
                    .supplier(maria)
                    .items(List.of(
                            BookingItem.builder().sku("SKU-005").description("Maquinaria agrícola").quantity(3).unitPrice(new BigDecimal("12000.00")).totalAmount(new BigDecimal("36000.00")).build(),
                            BookingItem.builder().sku("SKU-006").description("Repuestos varios").quantity(50).unitPrice(new BigDecimal("240.00")).totalAmount(new BigDecimal("12000.00")).build()
                    ))
                    .build());

            // Maria - CANCELLED
            bookingRepository.save(BookingRequest.builder()
                    .bookingCode("BK-2024-004")
                    .issueDate(LocalDate.of(2024, 3, 15))
                    .expirationDate(LocalDate.of(2024, 7, 15))
                    .currency("EUR")
                    .incotermCode(IncotermCode.DDP)
                    .freightMode(FreightMode.ROAD)
                    .originCountry("USA")
                    .destinationCountry("Argentina")
                    .fobValue(new BigDecimal("9500.00"))
                    .status(BookingStatus.CANCELLED)
                    .createdAt(LocalDateTime.of(2024, 3, 15, 14, 0))
                    .active(true)
                    .supplier(maria)
                    .items(List.of(
                            BookingItem.builder().sku("SKU-007").description("Lubricantes industriales").quantity(100).unitPrice(new BigDecimal("95.00")).totalAmount(new BigDecimal("9500.00")).build()
                    ))
                    .build());

            // Diego - DRAFT
            bookingRepository.save(BookingRequest.builder()
                    .bookingCode("BK-2024-005")
                    .issueDate(LocalDate.of(2024, 4, 20))
                    .expirationDate(LocalDate.of(2024, 10, 20))
                    .currency("EUR")
                    .incotermCode(IncotermCode.CFR)
                    .freightMode(FreightMode.SEA)
                    .originCountry("Germany")
                    .destinationCountry("Argentina")
                    .fobValue(new BigDecimal("27500.00"))
                    .status(BookingStatus.DRAFT)
                    .createdAt(LocalDateTime.of(2024, 4, 20, 11, 0))
                    .active(true)
                    .supplier(diego)
                    .items(List.of(
                            BookingItem.builder().sku("SKU-008").description("Válvulas hidráulicas").quantity(150).unitPrice(new BigDecimal("110.00")).totalAmount(new BigDecimal("16500.00")).build(),
                            BookingItem.builder().sku("SKU-009").description("Bombas centrífugas").quantity(10).unitPrice(new BigDecimal("800.00")).totalAmount(new BigDecimal("8000.00")).build(),
                            BookingItem.builder().sku("SKU-010").description("Filtros de presión").quantity(50).unitPrice(new BigDecimal("60.00")).totalAmount(new BigDecimal("3000.00")).build()
                    ))
                    .build());
        }
    }
}
