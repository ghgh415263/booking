package com.example.booking.domain.payment;

import com.example.booking.domain.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration
public class PaymentMethodServiceConfig {

    @Bean
    public PointPaymentStrategy pointPaymentStrategy(MemberPointRepository memberPointRepository) {
        return new PointPaymentStrategy(memberPointRepository);
    }

    @Bean
    public PgPaymentStrategy pgPaymentStrategy() {
        return new PgPaymentStrategy();
    }

    @Bean
    public Map<Set<PaymentMethod>, PaymentStrategy> paymentStrategies(
            PointPaymentStrategy pointPaymentStrategy,
            PgPaymentStrategy pgPaymentStrategy
    ) {

        return Map.of(
                Set.of(PaymentMethod.POINT),
                pointPaymentStrategy,
                Set.of(PaymentMethod.CARD),
                pgPaymentStrategy
        );
    }

    @Bean
    public PaymentMethodService paymentMethodService(
            Map<Set<PaymentMethod>, PaymentStrategy> paymentStrategies
    ) {

        return new PaymentMethodService(paymentStrategies);
    }
}
