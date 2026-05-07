package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductSummary(
        Long id,
        String name,
        BigDecimal price,
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
