package com.booknest.wallet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Statement> statements = new ArrayList<>();
}