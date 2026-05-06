package com.example.booking.application;

import com.example.booking.domain.MemberPoint;
import com.example.booking.domain.MemberPointRepository;
import com.example.booking.domain.Order;
import com.example.booking.domain.OrderRepository;
import com.example.booking.domain.payment.Payment;
import com.example.booking.domain.payment.PaymentRepository;
import com.example.booking.domain.payment.PointDataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfirmPaymentValidator {

    private final PaymentRepository paymentRepository;

    /**
     * PG 결제 승인 요청 전에 결제 정보를 검증하고
     * 결제 상태를 PROCESSING 으로 변경한다.
     *
     * <p>검증 내용:
     * <ol>
     *     <li>주문에 PG 결제(CARD, YPAY)가 존재하는지 확인한다.</li>
     *     <li>PG 승인 요청 금액과 주문 결제 금액이 일치하는지 검증한다.</li>
     * </ol>
     *
     * <p>검증이 완료되면 모든 결제 상태를 PROCESSING 으로 변경한다.
     *
     * @param request 결제 승인 요청 정보
     * @throws IllegalArgumentException
     * <ul>
     *     <li>PG 결제가 존재하지 않는 경우</li>
     *     <li>결제 금액이 일치하지 않는 경우</li>
     * </ul>
     */
    @Transactional
    public void updateOrderStatusForPayment(PaymentConfirmRequest request) {

        List<Payment> paymentList = paymentRepository.findByOrderId(request.orderId());
        Payment payment = paymentList.stream()
                .filter(Payment::isPgPayment)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("CARD 또는 YPAY 결제가 필요합니다"));

        if (payment.getAmount().compareTo(request.amount()) != 0) {
            throw new IllegalArgumentException("결제 금액이 주문과 맞지 않습니다.");
        }

        paymentList.forEach(Payment::markProcessing);
    }
}
