package com.OnlineBankingSystem.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankUserRepository extends JpaRepository<BankUser, Long> {

	boolean existsByUserName(String userName);

	Optional<BankUser> findByUserName(String userName);
}
