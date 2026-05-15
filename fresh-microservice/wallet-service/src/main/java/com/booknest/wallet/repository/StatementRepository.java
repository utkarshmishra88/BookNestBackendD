package com.booknest.wallet.repository;

import com.booknest.wallet.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatementRepository extends JpaRepository<Statement, Long> {
    List<Statement> findByWalletWalletIdOrderByTransactionDateDesc(Long walletId);
    boolean existsByWalletWalletIdAndRemarks(Long walletId, String remarks);
}