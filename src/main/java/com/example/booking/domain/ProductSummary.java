package com.example.booking.domain;

import java.time.LocalDateTime;

public record ProductSummary(
        Long id,
        String name,
        Long price,
        LocalDateTime checkInAt,
        LocalDateTime checkOutAt
) {
    public static ProductSummary from(Product product) {
        return new ProductSummary(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCheckInAt(),
                product.getCheckOutAt()
        );
    }
}
