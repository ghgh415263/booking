package com.example.booking.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private PaymentMethod method; // CARD, POINT

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private LocalDateTime approvedAt;

    public Payment(String orderId,
                   PaymentMethod method,
                   Long amount) {

        this.orderId = orderId;
        this.method = method;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
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
}
