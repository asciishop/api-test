package com.example.importsapi.infrastructure.adapter.in.rest.mapper;

import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.port.in.command.BookingItemData;
import com.example.importsapi.domain.port.in.command.CreateBookingCommand;
import com.example.importsapi.domain.port.in.command.UpdateBookingCommand;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.request.CreateBookingRequest;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.request.UpdateBookingRequest;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.response.BookingItemResponse;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.response.BookingResponse;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.response.SupplierResponse;
import org.springframework.stereotype.Component;

@Component
public class BookingRestMapper {

    public CreateBookingCommand toCommand(CreateBookingRequest request, String taxId) {
        return new CreateBookingCommand(
                request.bookingCode(),
                request.issueDate(),
                request.expirationDate(),
                request.currency(),
                request.incotermCode(),
                request.freightMode(),
                request.originCountry(),
                request.destinationCountry(),
                request.fobValue(),
                taxId,
                request.items().stream()
                        .map(i -> new BookingItemData(i.sku(), i.description(), i.quantity(), i.unitPrice()))
                        .toList()
        );
    }

    public UpdateBookingCommand toCommand(UpdateBookingRequest request) {
        return new UpdateBookingCommand(
                request.issueDate(),
                request.expirationDate(),
                request.fobValue(),
                request.currency()
        );
    }

    public BookingResponse toResponse(BookingRequest domain) {
        SupplierResponse supplierResponse = new SupplierResponse(
                domain.getSupplier().getId(),
                domain.getSupplier().getName(),
                domain.getSupplier().getTaxId(),
                domain.getSupplier().getCountry(),
                domain.getSupplier().getAddress(),
                domain.getSupplier().getContactEmail()
        );

        var itemResponses = domain.getItems().stream()
                .map(item -> new BookingItemResponse(
                        item.getId(),
                        item.getSku(),
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalAmount()))
                .toList();

        return new BookingResponse(
                domain.getId(),
                domain.getBookingCode(),
                domain.getIssueDate(),
                domain.getExpirationDate(),
                domain.getCurrency(),
                domain.getIncotermCode(),
                domain.getFreightMode(),
                domain.getOriginCountry(),
                domain.getDestinationCountry(),
                domain.getFobValue(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                supplierResponse,
                itemResponses
        );
    }
}
