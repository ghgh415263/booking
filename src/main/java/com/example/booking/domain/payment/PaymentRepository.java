package com.example.booking.domain.payment;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Payment> findByOrderId(String orderId);

    @Modifying
    @Query("UPDATE Payment p SET p.status = com.example.booking.domain.payment.PaymentStatus.SUCCESS, p.approvedAt = CURRENT_TIMESTAMP WHERE p.orderId = :orderId")
    int markAsSuccess(String orderId);

    @Modifying
    @Query("UPDATE Payment p SET p.status = com.example.booking.domain.payment.PaymentStatus.FAILED WHERE p.orderId = :orderId")
    int markAsFailed(String orderId);
}
