package com.example.booking.domain;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class MemberPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 회원 ID
     */
    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    /**
     * 현재 보유 포인트
     */
    @Column(name = "point", nullable = false)
    private Long point;

    /**
     * 낙관적 락을 위한 버전 필드.
     * 회원 포인트는 동일 사용자에 대한 동시 수정 가능성이 비교적 낮다고 판단하여
     * 낙관적 락 전략을 사용.
     */
    @Version
    private Long version;

    public MemberPoint(Long memberId) {
        this.memberId = memberId;
        this.point = 0L;
    }

    public void charge(long amount) {
        validateAmount(amount);
        this.point += amount;
    }

    public void use(long amount) {
        validateAmount(amount);

        if (this.point < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        this.point -= amount;
    }

    private void validateAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("포인트 금액은 0보다 커야 합니다.");
        }
    }

}
