package com.example.booking.domain.payment;

import com.example.booking.application.OrderCreateResponse;
import com.example.booking.domain.Order;

import java.util.List;

/**
 * 외부 PG 승인(Payment Gateway confirmation)이 필요한 결제 처리 전략 인터페이스
 *
 * <p>PG 결제처럼 즉시 최종 확정되지 않고,
 * 승인(confirm) API 이후 최종 상태가 결정되는 결제 수단에 대한 처리를 정의한다.
 *
 */
public interface ConfirmNeededPaymentStrategy {

    OrderCreateResponse pay(List<Payment> paymentList, long memberId, Order order);
}
