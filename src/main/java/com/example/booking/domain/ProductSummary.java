package com.example.booking.domain;

import java.time.LocalDateTime;

public record ProductSummay(
        Long id,
        String name,
        Long price,
        LocalDateTime checkInAt,
        LocalDateTime checkOutAt
) {
}
