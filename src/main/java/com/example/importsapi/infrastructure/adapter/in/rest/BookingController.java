package com.example.importsapi.infrastructure.adapter.in.rest;

import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.model.PageResult;
import com.example.importsapi.domain.model.enums.BookingStatus;
import com.example.importsapi.domain.model.enums.FreightMode;
import com.example.importsapi.domain.port.in.ChangeBookingStatusUseCase;
import com.example.importsapi.domain.port.in.CreateBookingUseCase;
import com.example.importsapi.domain.port.in.DeleteBookingUseCase;
import com.example.importsapi.domain.port.in.GetBookingUseCase;
import com.example.importsapi.domain.port.in.ListBookingsUseCase;
import com.example.importsapi.domain.port.in.UpdateBookingUseCase;
import com.example.importsapi.domain.port.in.command.BookingFilterQuery;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.request.ChangeStatusRequest;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.request.CreateBookingRequest;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.request.UpdateBookingRequest;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.response.BookingResponse;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.response.PageResponse;
import com.example.importsapi.infrastructure.adapter.in.rest.mapper.BookingRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Requests", description = "API para gestionar solicitudes de reserva de importaciones")
public class BookingController {

    private final CreateBookingUseCase createBookingUseCase;
    private final GetBookingUseCase getBookingUseCase;
    private final ListBookingsUseCase listBookingsUseCase;
    private final UpdateBookingUseCase updateBookingUseCase;
    private final ChangeBookingStatusUseCase changeBookingStatusUseCase;
    private final DeleteBookingUseCase deleteBookingUseCase;
    private final BookingRestMapper mapper;

    @GetMapping
    @Operation(summary = "Listar solicitudes de reserva con filtros y paginación")
    public ResponseEntity<PageResponse<BookingResponse>> listBookings(
            Authentication auth,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) FreightMode freightMode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String bookingCode,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        BookingFilterQuery query = new BookingFilterQuery(
                auth.getName(), status, freightMode, dateFrom, dateTo, bookingCode, page, size);
        PageResult<BookingRequest> result = listBookingsUseCase.listBookings(query);

        return ResponseEntity.ok(new PageResponse<>(
                result.content().stream().map(mapper::toResponse).toList(),
                result.pageNumber(),
                result.pageSize(),
                result.totalElements(),
                result.totalPages()
        ));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva solicitud de reserva")
    public ResponseEntity<BookingResponse> createBooking(
            Authentication auth,
            @Valid @RequestBody CreateBookingRequest request) {
        BookingRequest created = createBookingUseCase.createBooking(mapper.toCommand(request, auth.getName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una solicitud de reserva por ID")
    public ResponseEntity<BookingResponse> getBooking(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(getBookingUseCase.getBooking(id, auth.getName())));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar una solicitud de reserva en estado DRAFT (issueDate, expirationDate, fobValue, currency)")
    public ResponseEntity<BookingResponse> updateBooking(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingRequest request) {
        BookingRequest updated = updateBookingUseCase.updateBooking(id, mapper.toCommand(request), auth.getName());
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar el estado de una solicitud de reserva")
    public ResponseEntity<BookingResponse> changeStatus(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request) {
        BookingRequest updated = changeBookingStatusUseCase.changeStatus(id, request.status(), auth.getName());
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una solicitud de reserva en estado DRAFT o CANCELADO")
    public ResponseEntity<Void> deleteBooking(Authentication auth, @PathVariable Long id) {
        deleteBookingUseCase.deleteBooking(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
