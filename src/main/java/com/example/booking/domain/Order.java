package com.example.booking.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderPayment implements Persistable<String> {

    @Id
    @Column(name = "order_id", length = 50)
    private String orderId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 50)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(updatable = false, insertable = false, nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    public OrderPayment(Long memberId, BigDecimal totalAmount, String idempotencyKey) {
        this.orderId = UUID.randomUUID().toString();
        this.idempotencyKey = idempotencyKey;
        this.memberId = memberId;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.READY;
    }

    public void markPgNeed() {
        this.status = OrderStatus.PG_NEED;
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }

    public void markProcessing() {
        if (status == OrderStatus.PAID)
            throw new IllegalStateException("이미 지불된 주문은 결제진행이 불가능합니다.");
        this.status = OrderStatus.PROCESSING;
    }

    public boolean canProceedPgPayment(){
        return status == OrderStatus.PG_NEED;
    }

    @Override
    public String getId() {
        return orderId;
    }

    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
