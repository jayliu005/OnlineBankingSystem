package com.OnlineBankingSystem.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
		Long id,
		String accountType,
		BigDecimal accountBalance,
		LocalDateTime dateOfCreated) {

	static AccountResponse from(BankAccount account) {
		return new AccountResponse(
				account.getId(),
				account.getAccountType(),
				account.getAccountBalance(),
				account.getDateOfCreated());
	}
}
