package com.example.booking.domain.payment;

import com.example.booking.application.PaymentConfirmRequest;
import com.example.booking.application.PaymentConfirmResult;

import java.util.List;

/**
 * 결제 승인(confirm) 후처리 전략 인터페이스.
 *
 * <p>결제 수단 조합에 따라 결제 승인 완료 처리,
 * 주문 상태 변경, 재고 확정 등의 로직을 수행한다.
 */
public interface PaymentConfirmStrategy {

    PaymentConfirmResult confirmPayment(List<Payment> paymentList,
                                        PgClientConfirmResponse pgClientConfirmResponse,
                                        PaymentConfirmRequest request,
                                        long memberId);
}
