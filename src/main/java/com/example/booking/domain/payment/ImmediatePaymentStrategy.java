package com.example.booking.domain.payment;

import com.example.booking.application.OrderCreateResponse;
import com.example.booking.domain.Order;

import java.util.List;

/**
 * 즉시 결제(Immediate Payment) 처리 전략 인터페이스
 *
 * <p>외부 PG 승인 없이 즉시 결제가 완료되는 결제 수단(예: 포인트 결제 등)에 대한
 * 결제 처리 로직을 정의한다.
 *
 */
public interface ImmediatePaymentStrategy {

    OrderCreateResponse pay(List<Payment> paymentList, long memberId, Order order, List<ReservedProduct> reservedProducts);
}
