package com.example.booking.domain.payment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private LocalDateTime approvedAt;

    private String paymentKey;

    private int failedCount;

    public Payment(String orderId,
                   PaymentMethod method,
                   BigDecimal amount) {

        this.orderId = orderId;
        this.method = method;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.failedCount = 0;
    }

    // ===== 상태 변경 =====

    public void markProcessing() {
        this.status = PaymentStatus.PROCESSING;
    }

    public void markSuccess() {
        this.status = PaymentStatus.SUCCESS;
        this.approvedAt = LocalDateTime.now();
    }

    public void markFail() {
        this.status = PaymentStatus.FAILED;
    }

    public boolean isPgPayment() {
        return method == PaymentMethod.CARD
                || method == PaymentMethod.YPAY;
    }

    public boolean isPointPayment() {
        return method == PaymentMethod.POINT;
    }

    public void markUnknown() {
        this.status = PaymentStatus.UNKNOWN;
        this.failedCount++;
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }
}
