package com.example.booking.application;

import com.example.booking.domain.OrderStatus;

public record OrderCreateResponse (String orderId, OrderStatus orderStatus) {
}
