package com.example.booking.application;

import com.example.booking.domain.*;
import com.example.booking.domain.payment.Payment;
import com.example.booking.domain.payment.PaymentMethodService;
import com.example.booking.domain.payment.PaymentRepository;
import jakarta.persistence.EntityManager;
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
public class OrderPaymentService {

    private final OrderPaymentRepository orderPaymentRepository;

    private final ProductRepository productRepository;

    private final OrderItemRepository orderItemRepository;

    private final PaymentMethodService paymentMethodService;

    private final PaymentRepository paymentRepository;

    private final EntityManager em;

    @Transactional
    public OrderCreateResponse create(OrderPaymentCreateRequest req, Long memberId) {
        return orderPaymentRepository.findByIdempotencyKey(req.idempotencyKey())
                .map(order -> new OrderCreateResponse(order.getOrderId(), order.getTotalAmount(), order.getStatus()))
                .orElseGet(() -> createNewOrderPayment(req, req.idempotencyKey(), memberId));
    }

    private OrderCreateResponse createNewOrderPayment(OrderPaymentCreateRequest req, String idempotencyKey, Long memberId) {
        List<Long> ids = req.items().stream()
                .map(OrderItemRequest::productId)
                .distinct()
                .toList();

        Map<Long, Product> productMap = productRepository.findByIdIn(ids)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        if (productMap.size() != ids.size()) {
            throw new ProductNotFoundException();
        }

        // 총액 계산
        BigDecimal totalPrice = req.items().stream()
                .map(item -> {
                    Product p = productMap.get(item.productId());
                    return BigDecimal.valueOf(p.getPrice())
                            .multiply(BigDecimal.valueOf(item.quantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 주문 저장
        OrderPayment orderPayment = new OrderPayment(memberId, totalPrice, idempotencyKey);
        try {
            orderPaymentRepository.saveAndFlush(orderPayment);
        } catch (DataIntegrityViolationException e) {
            return orderPaymentRepository.findByIdempotencyKey(idempotencyKey)
                    .map(o -> new OrderCreateResponse(o.getOrderId(), o.getTotalAmount(), o.getStatus()))
                    .orElseThrow(() -> new OrderNotFoundException(e));
        }
        boolean managed = em.contains(orderPayment);

        // 주문아이템 생성
        List<OrderItem> items = req.items().stream()
                .map(item -> {
                    Product product = productMap.get(item.productId());
                    return new OrderItem(
                            orderPayment.getOrderId(),
                            product.getId(),
                            product.getName(),
                            BigDecimal.valueOf(product.getPrice()),
                            item.quantity()
                    );
                })
                .toList();

        orderItemRepository.saveAll(items);

        List<Payment> paymentList = req.payments().stream()
                .map(p -> new Payment(orderPayment.getOrderId(), p.type(), p.amount()))
                .toList();

        paymentMethodService.proceed(paymentList, memberId, orderPayment);

        paymentRepository.saveAll(paymentList);

        return new OrderCreateResponse(orderPayment.getOrderId(), orderPayment.getTotalAmount(), orderPayment.getStatus());
    }
}
