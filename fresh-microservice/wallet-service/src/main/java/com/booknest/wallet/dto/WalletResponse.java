package com.booknest.wallet.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class WalletResponse {
    private Long walletId;
    private Long userId;
    private BigDecimal balance;
    private List<StatementResponse> statements;
}