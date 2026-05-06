package com.example.booking.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final Map<Set<PaymentMethod>, PaymentStrategy> paymentStrategies;

    public PaymentMethodResult pay(List<Payment> paymentList, long memberId) {

        Set<PaymentMethod> paymentMethodSet = paymentList.stream().map(Payment::getMethod)
                .collect(Collectors.toSet());

        PaymentStrategy paymentStrategy = paymentStrategies.get(paymentMethodSet);
        if (paymentStrategy == null) {
            throw new NotSupportedPaymentMethodException();
        }
        return paymentStrategy.pay(paymentList, memberId);
    }
}
