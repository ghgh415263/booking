package com.example.booking.application;

import com.example.booking.domain.payment.PaymentStatus;

public record PaymentConfirmResult(
        PaymentStatus paymentStatus,
        String message
) {
}
