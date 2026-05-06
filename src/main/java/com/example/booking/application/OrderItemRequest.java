package com.example.booking.application;

public record OrderItemRequest(
        Long productId,
        int quantity
) {}
