package com.example.booking.application;

import com.example.booking.domain.payment.PaymentMethod;

import java.math.BigDecimal;

public record PaymentMethodRequest(
        PaymentMethod type,
        BigDecimal amount
) {}
