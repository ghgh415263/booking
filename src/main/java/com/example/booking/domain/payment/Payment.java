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

    @Column(unique = true)
    private String paymentKey;

    private int failedCount;

    private String failReason;

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
        if (this.status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("이미 성공한 요청입니다.");
        }
        if (this.status == PaymentStatus.FAILED) {
            throw new IllegalStateException("이미 실패한 요청입니다.");
        }
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 처리가 가능한 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.status = PaymentStatus.PROCESSING;
    }

    public void markSuccess() {
        this.status = PaymentStatus.SUCCESS;
        this.approvedAt = LocalDateTime.now();
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
