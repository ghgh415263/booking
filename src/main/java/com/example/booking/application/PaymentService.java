package com.example.booking.application;

import com.example.booking.domain.payment.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentConfirmService paymentConfirmService;

    private final ConfirmPaymentValidator confirmPaymentValidator;

    private final PgClient pgClient;

    /**
     * PG 결제 승인 요청을 처리한다.
     *
     * <p>처리 순서:
     * <ol>
     *     <li>결제 가능 상태인지 검증하고 주문 상태를 PROCESSING으로 갱신한다.</li>
     *     <li>PG사에 결제 승인(confirm)을 요청한다.</li>
     *     <li>결제 승인 결과에 따라 주문/결제/재고 상태를 최종 반영한다.</li>
     * </ol>
     *
     * @param request 결제 승인 요청 정보
     * @param memberId 결제를 요청한 회원 ID
     * @return 결제 승인 결과
     */
    public PaymentConfirmResult confirmPayment(PaymentConfirmRequest request, Long memberId) {

        confirmPaymentValidator.updateOrderStatusForPayment(request);

        PgClientConfirmResponse pgClientConfirmResponse = pgClient.confirmPayment(request);

        return paymentConfirmService.confirmPayment(pgClientConfirmResponse, request, memberId);
    }
}
