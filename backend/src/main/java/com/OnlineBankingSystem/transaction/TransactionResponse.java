package com.OnlineBankingSystem.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
		Long id,
		String transactionType,
		BigDecimal transactionAmount,
		Long accountFromId,
		Long accountToId,
		LocalDateTime transactionTime) {

	static TransactionResponse from(TransactionRecord record) {
		return new TransactionResponse(
				record.getId(),
				record.getTransactionType(),
				record.getTransactionAmount(),
				record.getAccountFrom().getId(),
				record.getAccountTo() == null ? null : record.getAccountTo().getId(),
				record.getTransactionTime());
	}
}
