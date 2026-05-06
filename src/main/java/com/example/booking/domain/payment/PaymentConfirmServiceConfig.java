package com.example.booking.domain.payment;

import com.example.booking.domain.MemberPointRepository;
import com.example.booking.domain.OrderItemRepository;
import com.example.booking.domain.OrderRepository;
import com.example.booking.domain.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

/**
 * 결제 승인(confirm) 처리 관련 Bean 설정 클래스.
 *
 * 결제 수단 조합에 따라 적절한 결제 승인 전략을 등록하고,
 * PaymentConfirmService 를 구성한다.
 */
@Configuration
public class PaymentConfirmServiceConfig {


    /**
     * 결제 승인 서비스 Bean 을 생성한다.
     *
     * @param paymentConfirmStrategies 결제 수단 조합별 승인 전략
     * @param paymentRepository 결제 저장소
     * @return 결제 승인 서비스
     */
    @Bean
    public PaymentConfirmService paymentConfirmService(Map<Set<PaymentMethod>, PaymentConfirmStrategy> paymentConfirmStrategies,
                                                       PaymentRepository paymentRepository) {
        return new PaymentConfirmService(paymentConfirmStrategies, paymentRepository);
    }

    @Bean
    public Map<Set<PaymentMethod>, PaymentConfirmStrategy> paymentConfirmStrategies(PaymentConfirmStrategy pointAndPgPaymentConfirmStrategy,
                                                                                    PaymentConfirmStrategy pgPaymentConfirmStrategy) {
        return Map.of(
                Set.of(PaymentMethod.CARD),
                pgPaymentConfirmStrategy,
                Set.of(PaymentMethod.YPAY),
                pgPaymentConfirmStrategy,
                Set.of(PaymentMethod.POINT, PaymentMethod.CARD),
                pointAndPgPaymentConfirmStrategy,
                Set.of(PaymentMethod.POINT, PaymentMethod.YPAY),
                pointAndPgPaymentConfirmStrategy
        );
    }

    @Bean
    public PaymentConfirmStrategy pointAndPgPaymentConfirmStrategy(OrderRepository orderRepository,
                                                                   MemberPointRepository memberPointRepository,
                                                                   PaymentRepository paymentRepository,
                                                                   OrderItemRepository orderItemRepository,
                                                                   ProductRepository productRepository) {
        return new PointAndPgPaymentConfirmStrategy(orderRepository, memberPointRepository, paymentRepository, orderItemRepository, productRepository);
    }

    @Bean
    public PaymentConfirmStrategy pgPaymentConfirmStrategy(OrderRepository orderRepository,
                                                           PaymentRepository paymentRepository,
                                                           OrderItemRepository orderItemRepository,
                                                           ProductRepository productRepository) {
        return new PgPaymentConfirmStrategy(orderRepository, paymentRepository, orderItemRepository, productRepository);
    }

}
