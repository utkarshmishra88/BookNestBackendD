package com.booknest.order.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private BigDecimal discountPercentage;

    @Column(nullable = false)
    private BigDecimal maxDiscountAmount;

    @Column(nullable = false)
    private BigDecimal minOrderAmount;

    private boolean active;

    private LocalDate expiryDate;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
