package com.example.booking.infra;

import com.example.booking.application.PaymentConfirmRequest;
import com.example.booking.domain.payment.PgClient;
import com.example.booking.domain.payment.PgClientConfirmResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class MyPgPaymentClient implements PgClient {

    private final String mode;

    public MyPgPaymentClient(@Value("${pg.mode:SUCCESS}") String mode) {
        this.mode = mode;
    }

    @Override
    public PgClientConfirmResponse confirmPayment(PaymentConfirmRequest request) {

        return switch (mode) {

            case "SUCCESS" -> new PgClientConfirmResponse(
                    true,
                    false,
                    false,
                    "SUCCESS"
            );

            case "RETRY" -> new PgClientConfirmResponse(
                    false,
                    false,
                    true,
                    "RETRYABLE"
            );

            case "FAIL" -> new PgClientConfirmResponse(
                    false,
                    true,
                    false,
                    "FAILED"
            );

            default -> throw new IllegalArgumentException("unknown pg.mode: " + mode);
        };
    }
}
