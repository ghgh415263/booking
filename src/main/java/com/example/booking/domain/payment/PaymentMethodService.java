package com.example.booking.domain.payment;

import com.example.booking.application.OrderCreateResponse;
import com.example.booking.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final Map<Set<PaymentMethod>, ConfirmNeededPaymentStrategy> confirmNeededPaymentStrategies;

    private final Map<Set<PaymentMethod>, ImmediatePaymentStrategy> immediatePaymentStrategies;

    /**
     * 결제 수단 조합에 따라 결제 처리 전략을 선택하여 실행한다.
     *
     * <p>처리 방식:
     * <ul>
     *     <li>즉시 결제(IMMEDIATE): 포인트 등 즉시 차감 가능한 결제 처리</li>
     *     <li>승인 필요 결제(CONFIRM): PG 승인 등 외부 결제 후처리 필요</li>
     * </ul>
     *
     * <p>결제 수단 조합(Set<PaymentMethod>)을 기준으로 전략을 선택한다.
     * 매핑되지 않은 조합은 예외 처리한다.
     *
     * @param paymentList 결제 목록
     * @param memberId 회원 ID
     * @param order 주문 정보
     * @param reservedProducts 예약된 상품 정보
     * @return 결제 처리 결과
     * @throws NotSupportedPaymentMethodException 지원하지 않는 결제 조합일 경우
     */
    public OrderCreateResponse proceed(List<Payment> paymentList, long memberId, Order order, List<ReservedProduct> reservedProducts) {

        Set<PaymentMethod> paymentMethodSet = paymentList.stream()
                .map(Payment::getMethod)
                .collect(Collectors.toSet());

        ImmediatePaymentStrategy immediatePaymentStrategy = immediatePaymentStrategies.get(paymentMethodSet);

        if (immediatePaymentStrategy != null) {
            return immediatePaymentStrategy.pay(paymentList, memberId, order, reservedProducts);
        }

        ConfirmNeededPaymentStrategy confirmNeededPaymentStrategy = confirmNeededPaymentStrategies.get(paymentMethodSet);

        if (confirmNeededPaymentStrategy != null) {
            return confirmNeededPaymentStrategy.pay(paymentList, memberId, order);
        }

        throw new NotSupportedPaymentMethodException();
    }
}
