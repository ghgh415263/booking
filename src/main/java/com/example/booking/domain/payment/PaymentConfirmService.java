package com.example.booking.domain.payment;

import com.example.booking.application.PaymentConfirmRequest;
import com.example.booking.application.PaymentConfirmResult;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PaymentConfirmService {

    private final Map<Set<PaymentMethod>, PaymentConfirmStrategy> paymentConfirmStrategies;

    private final PaymentRepository paymentRepository;

    /**
     * 결제 수단 조합에 맞는 결제 승인 전략을 선택하여
     * 결제 승인 후처리를 수행한다.
     *
     * <p>처리 내용:
     * <ol>
     *     <li>주문에 연결된 결제 정보를 조회한다.</li>
     *     <li>결제 수단 조합으로 승인 전략을 결정한다.</li>
     *     <li>전략에 따라 결제 승인 완료 처리 및 재고 확정을 수행한다.</li>
     * </ol>
     *
     * @param pgClientConfirmResponse PG 결제 승인 응답
     * @param request 결제 승인 요청 정보
     * @param memberId 결제를 요청한 회원 ID
     * @return 결제 승인 처리 결과
     * @throws NotSupportedPaymentMethodException 지원하지 않는 결제 수단 조합인 경우
     */
    @Transactional
    public PaymentConfirmResult confirmPayment(PgClientConfirmResponse pgClientConfirmResponse, PaymentConfirmRequest request, long memberId) {

        List<Payment> paymentList = paymentRepository.findByOrderId(request.orderId());

        Set<PaymentMethod> paymentMethodSet = paymentList.stream().map(Payment::getMethod)
                .collect(Collectors.toSet());

        PaymentConfirmStrategy paymentConfirmStrategy = paymentConfirmStrategies.get(paymentMethodSet);
        if (paymentConfirmStrategy == null) {
            throw new NotSupportedPaymentMethodException();
        }
        return paymentConfirmStrategy.confirmPayment(paymentList, pgClientConfirmResponse, request, memberId);
    }
}
