package com.example.booking.domain.payment;

import com.example.booking.application.OrderCreateResponse;
import com.example.booking.domain.MemberPoint;
import com.example.booking.domain.MemberPointRepository;
import com.example.booking.domain.Order;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class PointAndPgPaymentStrategy implements PaymentStrategy {

    private final MemberPointRepository memberPointRepository;

    @Override
    public OrderCreateResponse pay(List<Payment> paymentList, long memberId, Order order, List<ReservedProduct> reservedProducts) {
        BigDecimal paymentTotalAmount = paymentList.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!order.getTotalAmount().equals(paymentTotalAmount)) {
            throw new IllegalArgumentException("주문 총가격과 지불 총가격이 맞지않습니다.");
        }
        paymentList.stream()
                .filter(Payment::isPointPayment)
                .findFirst()
                .ifPresent(pointPayment -> {
                    MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId)
                            .orElseThrow(PointDataNotFoundException::new);
                    memberPoint.reserve(pointPayment.getAmount());
                });
        return new OrderCreateResponse(order.getOrderId(), order.getStatus());
    }
}
