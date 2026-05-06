package com.example.booking.application;

import java.util.List;

public record OrderPaymentCreateRequest(
        String idempotencyKey,
        List<OrderItemRequest> items,
        List<PaymentMethodRequest> payments
) {}
