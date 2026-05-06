package com.example.booking.domain.payment;

import com.example.booking.domain.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration
public class PaymentMethodServiceConfig {

    @Bean
    public PaymentMethodService paymentMethodService(Map<Set<PaymentMethod>, ConfirmNeededPaymentStrategy> confirmNeededPaymentStrategies,
                                                     Map<Set<PaymentMethod>, ImmediatePaymentStrategy> immediatePaymentStrategies) {
        return new PaymentMethodService(confirmNeededPaymentStrategies, immediatePaymentStrategies);
    }

    @Bean
    public Map<Set<PaymentMethod>, ImmediatePaymentStrategy> immediatePaymentStrategies(
            ImmediatePaymentStrategy pointImmediatePaymentStrategy
    ) {
        return Map.of(
                Set.of(PaymentMethod.POINT),
                pointImmediatePaymentStrategy
        );
    }

    @Bean
    public ImmediatePaymentStrategy pointImmediatePaymentStrategy(
            MemberPointRepository memberPointRepository
    ) {
        return new PointImmediatePaymentStrategy(memberPointRepository);
    }


    @Bean
    public Map<Set<PaymentMethod>, ConfirmNeededPaymentStrategy> confirmNeededPaymentStrategies(
            ConfirmNeededPaymentStrategy pgConfirmNeededPaymentStrategy,
            ConfirmNeededPaymentStrategy pointAndPgConfirmNeededPaymentStrategy) {
        return Map.of(
                Set.of(PaymentMethod.CARD),
                pgConfirmNeededPaymentStrategy,
                Set.of(PaymentMethod.YPAY),
                pgConfirmNeededPaymentStrategy,
                Set.of(PaymentMethod.POINT, PaymentMethod.CARD),
                pointAndPgConfirmNeededPaymentStrategy,
                Set.of(PaymentMethod.POINT, PaymentMethod.YPAY),
                pointAndPgConfirmNeededPaymentStrategy
        );
    }

    @Bean
    public ConfirmNeededPaymentStrategy pgConfirmNeededPaymentStrategy() {
        return new PgConfirmNeededPaymentStrategy();
    }

    @Bean
    public ConfirmNeededPaymentStrategy pointAndPgConfirmNeededPaymentStrategy(MemberPointRepository memberPointRepository) {
        return new PointAndPgConfirmNeededPaymentStrategy(memberPointRepository);
    }
}
