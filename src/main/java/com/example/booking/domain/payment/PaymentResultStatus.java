package com.example.booking.domain.payment;

public enum PaymentResultStatus {

    /**
     * 추가 결제 요청(PG 호출 등)이 필요하지 않다.
     */
    FINISHED,

    /**
     * 외부 PG 결제 요청이 필요한 상태.
     * 예: 카드 결제 승인 요청 필요
     */
    PG_REQUIRED
}
