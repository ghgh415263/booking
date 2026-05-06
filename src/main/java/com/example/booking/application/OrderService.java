package com.example.booking.application;

import com.example.booking.domain.*;
import com.example.booking.domain.payment.Payment;
import com.example.booking.domain.payment.PaymentMethodService;
import com.example.booking.domain.payment.PaymentRepository;
import com.example.booking.domain.payment.ReservedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final OrderItemRepository orderItemRepository;

    private final PaymentMethodService paymentMethodService;

    private final PaymentRepository paymentRepository;

    /**
     * 주문 생성 및 결제 초기 처리 로직
     *
     * <p>멱등키(idempotencyKey)를 기반으로 중복 주문 생성을 방지하며,
     * 주문 생성 → 재고 예약 → 주문 아이템 생성 → 결제 정보 생성까지 수행한다.
     *
     * <p>이후 결제 방식(PG / 포인트 / 혼합)에 따라 PaymentStrategy로 위임한다.
     *
     * @param req 주문 생성 요청 정보
     * @param memberId 회원 ID
     * @return 주문 생성 및 결제 처리 결과
     */
    @Transactional
    public OrderCreateResponse create(OrderCreateRequest req, Long memberId) {
        return orderRepository.findByIdempotencyKey(req.idempotencyKey())
                .map(order -> new OrderCreateResponse(order.getOrderId(), order.getStatus()))
                .orElseGet(() -> createNewOrder(req, req.idempotencyKey(), memberId));
    }

    private OrderCreateResponse createNewOrder(OrderCreateRequest req, String idempotencyKey, Long memberId) {
        List<Long> productIds = req.items().stream()
                .map(OrderItemRequest::productId)
                .distinct()
                .toList();

        Map<Long, Product> productMap = productRepository.findByIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        if (productMap.size() != productIds.size()) {
            throw new ProductNotFoundException();
        }

        validateEventProducts(req.items(), productMap);

        req.items().forEach(item -> {
            Product product = productMap.get(item.productId());
            if (product == null) {
                throw new IllegalArgumentException( "상품이 존재하지 않습니다. productId=" + item.productId());
            }
            product.reserve(item.quantity());
        });

        // 총액 계산
        BigDecimal totalPrice = req.items().stream()
                .map(item -> {
                    Product p = productMap.get(item.productId());
                    return BigDecimal.valueOf(p.getPrice())
                            .multiply(BigDecimal.valueOf(item.quantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 주문 저장
        Order order = new Order(memberId, totalPrice, idempotencyKey);
        try {
            orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException e) {
            return orderRepository.findByIdempotencyKey(idempotencyKey)
                    .map(o -> new OrderCreateResponse(o.getOrderId(), o.getStatus()))
                    .orElseThrow(() -> new OrderNotFoundException(e));
        }

        // 주문아이템 생성
        List<OrderItem> items = req.items().stream()
                .map(item -> {
                    Product product = productMap.get(item.productId());
                    return new OrderItem(
                            order.getOrderId(),
                            product.getId(),
                            product.getName(),
                            BigDecimal.valueOf(product.getPrice()),
                            item.quantity()
                    );
                })
                .toList();

        orderItemRepository.saveAll(items);

        List<Payment> paymentList = req.payments().stream()
                .map(p -> new Payment(order.getOrderId(), p.type(), p.amount()))
                .toList();

        paymentRepository.saveAll(paymentList);

        List<ReservedProduct> reservedProducts = req.items().stream()
                .map(item -> new ReservedProduct(
                        productMap.get(item.productId()),
                        item.quantity()
                ))
                .toList();

        return paymentMethodService.proceed(paymentList, memberId, order, reservedProducts);
    }

    private void validateEventProducts(
            List<OrderItemRequest> items,
            Map<Long, Product> productMap
    ) {
        items.forEach(item -> {
            Product product = productMap.get(item.productId());

            if (product.isEventProduct() && item.quantity() > 1) {
                throw new IllegalStateException(
                        "이벤트 상품은 1개만 구매할 수 있습니다. productId=" + product.getId()
                );
            }
        });
    }
}
