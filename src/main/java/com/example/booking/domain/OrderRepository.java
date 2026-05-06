package com.example.booking.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Query("UPDATE Order o SET o.status = com.example.booking.domain.OrderStatus.CANCELED WHERE o.orderId = :orderId")
    int markAsCanceled(@Param("orderId") String orderId);

    @Modifying
    @Query("UPDATE Order o SET o.status = com.example.booking.domain.OrderStatus.PAID, o.paidAt = CURRENT_TIMESTAMP WHERE o.orderId = :orderId")
    int markAsPaid(@Param("orderId") String orderId);
}
