package com.OnlineBankingSystem.account;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

	List<BankAccount> findAllByBankUserIdOrderByIdAsc(Long userId);

	Optional<BankAccount> findByIdAndBankUserUserName(Long id, String userName);
}
