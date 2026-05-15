package com.booknest.payment.repository;

import com.booknest.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

    java.util.List<Payment> findAllByOrderByCreatedAtDesc();
}