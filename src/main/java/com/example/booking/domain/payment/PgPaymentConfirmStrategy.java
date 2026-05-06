package com.example.booking.domain.payment;

import com.example.booking.application.PaymentConfirmRequest;
import com.example.booking.application.PaymentConfirmResult;
import com.example.booking.domain.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PgPaymentConfirmStrategy implements PaymentConfirmStrategy{

    private final OrderRepository orderRepository;

    private final PaymentRepository paymentRepository;

    private final OrderItemRepository orderItemRepository;

    private final ProductRepository productRepository;

    /**
     * PG 단독 결제 승인 완료 후처리를 수행한다.
     *
     * <p>처리 순서:
     * <ol>
     *     <li>주문 상품(OrderItem) 정보를 조회한다.</li>
     *     <li>재고 확정을 위해 상품 정보를 조회한다.</li>
     *     <li>PG 결제 정보를 조회하고 paymentKey 를 저장한다.</li>
     *     <li>PG 승인 결과에 따라 주문/결제/재고 상태를 변경한다.</li>
     * </ol>
     *
     * <p>승인 결과 처리:
     * <ul>
     *     <li>SUCCESS:
     *          주문 완료 처리,
     *          결제 SUCCESS 처리,
     *          예약 재고 확정(confirm)</li>
     *     <li>RETRIABLE:
     *          결제 상태를 UNKNOWN 으로 변경</li>
     *     <li>FAILED:
     *          주문 취소 처리,
     *          결제 FAILED 처리</li>
     * </ul>
     *
     * @param paymentList 주문 결제 목록
     * @param pgClientConfirmResponse PG 결제 승인 응답
     * @param request 결제 승인 요청 정보
     * @param memberId 결제를 요청한 회원 ID
     * @return 결제 승인 결과
     */
    @Override
    public PaymentConfirmResult confirmPayment(List<Payment> paymentList, PgClientConfirmResponse pgClientConfirmResponse, PaymentConfirmRequest request, long memberId) {

        List<OrderItem> orderItems =
                orderItemRepository.findByOrderId(request.orderId());

        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .toList();

        Map<Long, Product> productMap =
                productRepository.findByIdIn(productIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Product::getId,
                                Function.identity()
                        ));

        Payment payment = paymentList.stream()
                .filter(Payment::isPgPayment)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("CARD 또는 YPAY 결제가 필요합니다"));

        payment.setPaymentKey(request.paymentKey());
        if (pgClientConfirmResponse.isSuccess()) {
            orderRepository.markAsPaid(request.orderId());
            paymentRepository.markAsSuccess(request.orderId());
            orderItems.forEach(item -> {
                Product product = productMap.get(item.getProductId());
                product.confirmPurchase(item.getQuantity());
            });
            return new PaymentConfirmResult(PaymentStatus.SUCCESS, pgClientConfirmResponse.message());
        } else if (pgClientConfirmResponse.isRetriable()) {
            payment.markUnknown();
            return new PaymentConfirmResult(PaymentStatus.UNKNOWN, pgClientConfirmResponse.message());
        } else {
            orderItems.forEach(item -> {
                Product product = productMap.get(item.getProductId());
                product.releaseReserve(item.getQuantity());
            });
            orderRepository.markAsCanceled(request.orderId());
            paymentRepository.markAsFailed(request.orderId());
            return new PaymentConfirmResult(PaymentStatus.FAILED, pgClientConfirmResponse.message());
        }
    }
}
