package com.example.booking.domain.payment;

public record PgClientConfirmResponse(
        boolean isSuccess,
        boolean isFail,
        boolean isRetriable,
        String message
) {
}
