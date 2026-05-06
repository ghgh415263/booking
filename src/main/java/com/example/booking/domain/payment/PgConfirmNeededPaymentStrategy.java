package com.example.booking.domain.payment;

import com.example.booking.application.OrderCreateResponse;
import com.example.booking.domain.Order;

import java.math.BigDecimal;
import java.util.List;

public class PgPaymentStrategy implements PaymentStrategy {

    @Override
    public OrderCreateResponse pay(List<Payment> paymentList, long memberId, Order order, List<ReservedProduct> reservedProducts) {

        BigDecimal paymentTotalAmount = paymentList.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!order.getTotalAmount().equals(paymentTotalAmount)) {
            throw new IllegalArgumentException("주문 총가격과 지불 총가격이 맞지않습니다.");
        }

        return new OrderCreateResponse(order.getOrderId(), order.getStatus());
    }
}
