package com.booknest.wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "statements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statementId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, length = 20)
    private String transactionType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @PrePersist
    public void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
}