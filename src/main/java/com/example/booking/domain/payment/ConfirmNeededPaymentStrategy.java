package com.example.booking.domain.payment;

import com.example.booking.application.OrderCreateResponse;
import com.example.booking.domain.Order;

import java.util.List;

public interface PaymentStrategy {

    OrderCreateResponse pay(List<Payment> paymentList, long memberId, Order order, List<ReservedProduct> reservedProducts);
}
