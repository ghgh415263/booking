package com.example.booking.ui;

import com.example.booking.domain.PaymentMethod;

public record PaymentMethodRequest(
        PaymentMethod type,
        Long amount
) {}
