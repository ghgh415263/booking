package com.example.booking.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 그룹 식별자: 검색이 잦으므로 인덱스 부여 및 길이 제한
    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    // 총액: 단가 * 수량 결과값 저장
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    public OrderItem(String orderId,
                     Long productId,
                     String productName,
                     BigDecimal price,
                     int quantity) {

        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
    }
}
