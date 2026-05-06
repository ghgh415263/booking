package com.example.booking.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OrderItem>  findByOrderId(String orderId);
}
