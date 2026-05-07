package com.example.booking.domain.payment;

import com.example.booking.application.OrderCreateResponse;
import com.example.booking.domain.MemberPoint;
import com.example.booking.domain.MemberPointRepository;
import com.example.booking.domain.Order;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PointAndPgConfirmNeededPaymentStrategy implements ConfirmNeededPaymentStrategy {

    private final MemberPointRepository memberPointRepository;

    @Override
    public OrderCreateResponse pay(List<Payment> paymentList, long memberId, Order order) {

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
