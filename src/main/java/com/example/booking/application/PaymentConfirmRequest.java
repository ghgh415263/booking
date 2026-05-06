package com.example.booking.application;

import java.math.BigDecimal;

public record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        BigDecimal amount
) {}
