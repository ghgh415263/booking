package com.example.booking.domain.payment;

import com.example.booking.application.PaymentConfirmRequest;

public interface PgClient {

    /**
     * PG사에 결제 승인(confirm)을 요청한다.
     *
     * @param request 결제 승인 요청 정보
     * @return PG 결제 승인 응답
     */
    PgClientConfirmResponse confirmPayment(PaymentConfirmRequest request);
}
