package com.example.booking.domain.payment;

import com.example.booking.application.OrderCreateResponse;
import com.example.booking.domain.MemberPoint;
import com.example.booking.domain.MemberPointRepository;
import com.example.booking.domain.Order;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PointImmediatePaymentStrategy implements ImmediatePaymentStrategy {

    private final MemberPointRepository memberPointRepository;

    @Override
    public OrderCreateResponse pay(List<Payment> paymentList, long memberId, Order order, List<ReservedProduct> reservedProducts) {

        Payment payment = paymentList.get(0);

        MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId)
                .orElseThrow(PointDataNotFoundException::new);

        if (!order.getTotalAmount().equals(payment.getAmount())) {
            throw new IllegalArgumentException("주문 총가격과 지불 총가격이 맞지않습니다.");
        }
        memberPoint.deductImmediately(payment.getAmount());
        payment.markSuccess();
        order.markPaid();
        reservedProducts.forEach(rp ->
                rp.product().confirmPurchase(rp.quantity())
        );
        return new OrderCreateResponse(order.getOrderId(), order.getStatus());
    }
}
