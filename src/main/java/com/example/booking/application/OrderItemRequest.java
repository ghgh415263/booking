package com.example.booking.ui;

public record OrderItemRequest(
        Long productId,
        int quantity
) {}
