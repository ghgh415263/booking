package com.example.booking.application;

public record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        long amount
) {}
