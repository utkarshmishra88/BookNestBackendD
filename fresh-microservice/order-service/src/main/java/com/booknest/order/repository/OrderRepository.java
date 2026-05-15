package com.booknest.order.repository;

import com.booknest.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByOrderIdAndUserId(Long orderId, Long userId);

    List<Order> findAllByOrderByCreatedAtDesc();
}