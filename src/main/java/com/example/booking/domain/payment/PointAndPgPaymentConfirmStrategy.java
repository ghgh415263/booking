package com.example.booking.domain.payment;

import com.example.booking.application.PaymentConfirmRequest;
import com.example.booking.application.PaymentConfirmResult;
import com.example.booking.domain.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PointAndPgPaymentConfirmStrategy implements PaymentConfirmStrategy{

    private final OrderRepository orderRepository;
    private final MemberPointRepository memberPointRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    private EntityManager entityManager;

    @Autowired
    public void setEntityManager(EntityManager entityManager){
        this.entityManager = entityManager;
    }
    /**
     * 포인트 + PG 혼합 결제 승인 완료 후처리를 수행한다.
     *
     * <p>처리 순서:
     * <ol>
     *     <li>포인트 결제와 PG 결제를 분리한다.</li>
     *     <li>주문 상품(OrderItem) 및 상품 정보를 조회한다.</li>
     *     <li>PG paymentKey 를 저장한다.</li>
     *     <li>PG 승인 결과에 따라 포인트/주문/결제/재고 상태를 변경한다.</li>
     * </ol>
     *
     * <p>승인 결과 처리:
     * <ul>
     *     <li>SUCCESS:
     *          포인트 사용 확정,
     *          주문 완료 처리,
     *          결제 SUCCESS 처리,
     *          예약 재고 확정(confirm)</li>
     *     <li>RETRIABLE:
     *          PG 결제를 UNKNOWN 상태로 변경</li>
     *     <li>FAILED:
     *          포인트 사용 복구,
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

        Map<Boolean, List<Payment>> grouped = paymentList.stream()
                        .collect(Collectors.partitioningBy(Payment::isPointPayment));

        Payment pointPayment = grouped.get(true).get(0);
        Payment pgPayment = grouped.get(false).get(0);

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

        Product product22 = productMap.get(1l);
        System.out.println(entityManager.contains(product22));

        pgPayment.setPaymentKey(request.paymentKey());
        if (pgClientConfirmResponse.isSuccess()) {
            MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId)
                    .orElseThrow(PointDataNotFoundException::new);
            memberPoint.confirmUse(pointPayment.getAmount());
            orderRepository.markAsPaid(request.orderId());
            paymentRepository.markAsSuccess(request.orderId());
            orderItems.forEach(item -> {
                Product product = productMap.get(item.getProductId());
                product.confirmPurchase(item.getQuantity());
            });
            return new PaymentConfirmResult(PaymentStatus.SUCCESS, pgClientConfirmResponse.message());
        }
        else if (pgClientConfirmResponse.isRetriable()) {
            pgPayment.markUnknown();
            return new PaymentConfirmResult(PaymentStatus.UNKNOWN, pgClientConfirmResponse.message());
        }
        else {
            MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId)
                    .orElseThrow(PointDataNotFoundException::new);
            memberPoint.release(pointPayment.getAmount());
            System.out.println(entityManager.contains(product22));
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
