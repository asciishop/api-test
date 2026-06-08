package com.example.importsapi.application.service;

import com.example.importsapi.domain.exception.BookingNotFoundException;
import com.example.importsapi.domain.exception.DuplicateBookingCodeException;
import com.example.importsapi.domain.exception.InvalidBookingStateException;
import com.example.importsapi.domain.exception.InvalidStatusTransitionException;
import com.example.importsapi.domain.exception.SupplierNotFoundException;
import com.example.importsapi.domain.model.BookingItem;
import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.model.PageResult;
import com.example.importsapi.domain.model.Supplier;
import com.example.importsapi.domain.model.enums.BookingStatus;
import com.example.importsapi.domain.port.in.ChangeBookingStatusUseCase;
import com.example.importsapi.domain.port.in.CreateBookingUseCase;
import com.example.importsapi.domain.port.in.DeleteBookingUseCase;
import com.example.importsapi.domain.port.in.GetBookingUseCase;
import com.example.importsapi.domain.port.in.ListBookingsUseCase;
import com.example.importsapi.domain.port.in.UpdateBookingUseCase;
import com.example.importsapi.domain.port.in.command.BookingFilterQuery;
import com.example.importsapi.domain.port.in.command.CreateBookingCommand;
import com.example.importsapi.domain.port.in.command.UpdateBookingCommand;
import com.example.importsapi.domain.port.out.BookingRepositoryPort;
import com.example.importsapi.domain.port.out.SupplierRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService implements
        CreateBookingUseCase,
        GetBookingUseCase,
        ListBookingsUseCase,
        UpdateBookingUseCase,
        ChangeBookingStatusUseCase,
        DeleteBookingUseCase {

    private final BookingRepositoryPort bookingRepository;
    private final SupplierRepositoryPort supplierRepository;

    @Override
    @Transactional
    public BookingRequest createBooking(CreateBookingCommand command) {
        log.info("Creando reserva con código: {}", command.bookingCode());

        if (command.items() == null || command.items().isEmpty()) {
            throw new InvalidBookingStateException("La reserva debe tener al menos un ítem");
        }

        if (command.issueDate().isAfter(command.expirationDate())) {
            throw new InvalidBookingStateException("La fecha de emisión debe ser menor o igual a la fecha de vencimiento");
        }

        if (bookingRepository.existsByBookingCode(command.bookingCode())) {
            throw new DuplicateBookingCodeException(command.bookingCode());
        }

        Supplier supplier = supplierRepository.findByTaxId(command.taxId())
                .orElseThrow(() -> new SupplierNotFoundException(command.taxId()));

        List<BookingItem> items = command.items().stream()
                .map(itemData -> BookingItem.builder()
                        .sku(itemData.sku())
                        .description(itemData.description())
                        .quantity(itemData.quantity())
                        .unitPrice(itemData.unitPrice())
                        .totalAmount(itemData.unitPrice().multiply(java.math.BigDecimal.valueOf(itemData.quantity())))
                        .build())
                .toList();

        BookingRequest booking = BookingRequest.builder()
                .bookingCode(command.bookingCode())
                .issueDate(command.issueDate())
                .expirationDate(command.expirationDate())
                .currency(command.currency())
                .incotermCode(command.incotermCode())
                .freightMode(command.freightMode())
                .originCountry(command.originCountry())
                .destinationCountry(command.destinationCountry())
                .fobValue(command.fobValue())
                .status(BookingStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .active(true)
                .supplier(supplier)
                .items(items)
                .build();

        BookingRequest saved = bookingRepository.save(booking);
        log.info("Reserva creada con id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingRequest getBooking(Long id, String callerTaxId) {
        log.debug("Buscando reserva id: {}", id);
        BookingRequest booking = bookingRepository.findActiveById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        validateOwnership(id, booking, callerTaxId);
        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<BookingRequest> listBookings(BookingFilterQuery query) {
        log.debug("Listando reservas para taxId: {}, página: {}, tamaño: {}", query.taxId(), query.page(), query.size());
        return bookingRepository.findByFilter(query);
    }

    @Override
    @Transactional
    public BookingRequest updateBooking(Long id, UpdateBookingCommand command, String callerTaxId) {
        log.info("Actualizando reserva id: {}", id);
        BookingRequest existing = bookingRepository.findActiveById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        validateOwnership(id, existing, callerTaxId);

        if (existing.getStatus() != BookingStatus.DRAFT) {
            throw new InvalidBookingStateException("Solo las reservas en estado DRAFT pueden ser actualizadas");
        }

        LocalDate newIssueDate = command.issueDate() != null ? command.issueDate() : existing.getIssueDate();
        LocalDate newExpirationDate = command.expirationDate() != null ? command.expirationDate() : existing.getExpirationDate();

        if (newIssueDate.isAfter(newExpirationDate)) {
            throw new InvalidBookingStateException("La fecha de emisión debe ser menor o igual a la fecha de vencimiento");
        }

        BookingRequest updated = existing.toBuilder()
                .issueDate(newIssueDate)
                .expirationDate(newExpirationDate)
                .fobValue(command.fobValue() != null ? command.fobValue() : existing.getFobValue())
                .currency(command.currency() != null ? command.currency() : existing.getCurrency())
                .updatedAt(LocalDateTime.now())
                .build();

        BookingRequest saved = bookingRepository.save(updated);
        log.info("Reserva id: {} actualizada", id);
        return saved;
    }

    @Override
    @Transactional
    public BookingRequest changeStatus(Long id, BookingStatus newStatus, String callerTaxId) {
        log.info("Cambiando estado de reserva id: {} a {}", id, newStatus);
        BookingRequest existing = bookingRepository.findActiveById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        validateOwnership(id, existing, callerTaxId);

        if (!existing.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(existing.getStatus(), newStatus);
        }

        BookingRequest updated = existing.toBuilder().status(newStatus).updatedAt(LocalDateTime.now()).build();
        BookingRequest saved = bookingRepository.save(updated);
        log.info("Estado de reserva id: {} actualizado a {}", id, newStatus);
        return saved;
    }

    @Override
    @Transactional
    public void deleteBooking(Long id, String callerTaxId) {
        log.info("Eliminando reserva id: {}", id);
        BookingRequest existing = bookingRepository.findActiveById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        validateOwnership(id, existing, callerTaxId);

        if (existing.getStatus() == BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException("Solo las reservas en estado DRAFT o CANCELADO pueden eliminarse");
        }

        bookingRepository.save(existing.toBuilder().active(false).updatedAt(LocalDateTime.now()).build());
        log.info("Reserva id: {} eliminada (soft delete)", id);
    }

    private void validateOwnership(Long id, BookingRequest booking, String callerTaxId) {
        if (!booking.getSupplier().getTaxId().equals(callerTaxId)) {
            log.warn("Acceso denegado: taxId {} intentó acceder a reserva id: {}", callerTaxId, id);
            throw new BookingNotFoundException(id);
        }
    }
}
