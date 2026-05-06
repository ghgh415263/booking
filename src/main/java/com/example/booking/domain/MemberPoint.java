package com.example.booking.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity

public class MemberPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal reserved = BigDecimal.ZERO;

    public MemberPoint(Long memberId) {
        this.memberId = memberId;
        this.balance = BigDecimal.ZERO;
        this.reserved = BigDecimal.ZERO;
    }

    public void deductImmediately(BigDecimal amount) {
        validateAmount(amount);

        if (available().compareTo(amount) < 0) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        this.balance = this.balance.subtract(amount);
    }

    /**
     * 사용 가능 포인트
     */
    public BigDecimal available() {
        return balance.subtract(reserved);
    }

    /**
     * 포인트 예약 (PG 결제 전 잠금)
     */
    public void reserve(BigDecimal amount) {
        validateAmount(amount);

        if (available().compareTo(amount) < 0) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        this.reserved = this.reserved.add(amount);
    }

    /**
     * 결제 확정 (PG 성공 이후)
     */
    public void confirmUse(BigDecimal amount) {
        validateAmount(amount);

        if (reserved.compareTo(amount) < 0) {
            throw new IllegalStateException("예약된 포인트보다 큽니다.");
        }

        this.reserved = this.reserved.subtract(amount);
        this.balance = this.balance.subtract(amount);
    }

    /**
     * 예약 해제 (PG 실패 or 취소)
     */
    public void release(BigDecimal amount) {
        validateAmount(amount);

        if (reserved.compareTo(amount) < 0) {
            throw new IllegalStateException("예약된 포인트보다 큽니다.");
        }

        this.reserved = this.reserved.subtract(amount);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("포인트는 0보다 커야 합니다.");
        }
    }
}
