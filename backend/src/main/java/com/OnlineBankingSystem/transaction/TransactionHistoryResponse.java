package com.OnlineBankingSystem.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionHistoryResponse(
		Long id,
		LocalDateTime transactionTime,
		BigDecimal transactionAmount,
		String transactionNote) {

	static TransactionHistoryResponse from(TransactionRecord record, Long accountId) {
		String note;
		if ("Transfer".equals(record.getTransactionType())) {
			note = record.getAccountFrom().getId().equals(accountId)
					? "Send to Account ID " + record.getAccountTo().getId()
					: "Receive from Account ID " + record.getAccountFrom().getId();
		} else {
			note = record.getTransactionType() + " Money";
		}
		return new TransactionHistoryResponse(
				record.getId(),
			record.getTransactionTime(),
			record.getTransactionAmount(),
			note);
	}
}
