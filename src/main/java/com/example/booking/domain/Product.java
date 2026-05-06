package com.example.booking.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 상품명
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 판매 가격
     */
    @Column(nullable = false)
    private Long price;

    /**
     * 숙소/예약 상품 체크인 시간
     */
    @Column
    private LocalDateTime checkInAt;

    /**
     * 숙소/예약 상품 체크아웃 시간
     */
    @Column
    private LocalDateTime checkOutAt;

    @Column(nullable = false)
    private int totalStock;

    @Column(nullable = false)
    private int reservedStock;

    @Column(nullable = false)
    private boolean eventProduct;


    public Product(
            String name,
            Long price,
            LocalDateTime checkInAt,
            LocalDateTime checkOutAt,
            boolean eventProduct
    ) {
        validatePrice(price);

        this.name = name;
        this.price = price;
        this.checkInAt = checkInAt;
        this.checkOutAt = checkOutAt;
        this.eventProduct = eventProduct;
    }

    private void validatePrice(Long price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다.");
        }
    }

    /**
     * 현재 구매 가능한 재고
     */
    public int getAvailableStock() {
        return totalStock - reservedStock;
    }

    /**
     * 재고 선점(reserve)
     */
    public void reserve(int quantity) {
        validateQuantity(quantity);

        if (getAvailableStock() < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }

        this.reservedStock += quantity;
    }

    /**
     * 선점 재고 해제
     */
    public void releaseReserve(int quantity) {
        validateQuantity(quantity);

        if (reservedStock < quantity) {
            throw new IllegalStateException("예약 재고보다 많이 해제할 수 없습니다.");
        }

        this.reservedStock -= quantity;
    }

    /**
     * 결제 승인 후 실제 재고 차감
     */
    public void confirmPurchase(int quantity) {
        validateQuantity(quantity);

        if (reservedStock < quantity) {
            throw new IllegalStateException("예약 재고가 부족합니다.");
        }

        if (totalStock < quantity) {
            throw new IllegalStateException("전체 재고가 부족합니다.");
        }

        this.reservedStock -= quantity;
        this.totalStock -= quantity;
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
    }

}
