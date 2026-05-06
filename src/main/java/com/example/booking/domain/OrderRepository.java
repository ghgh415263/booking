package com.example.booking.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, String> {

    Optional<OrderPayment> findByIdempotencyKey(String idempotencyKey);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE OrderPayment o SET o.status = :status WHERE o.orderId = :orderId")
    int updateStatus(@Param("orderId") String orderId, @Param("status") OrderStatus status);
}
