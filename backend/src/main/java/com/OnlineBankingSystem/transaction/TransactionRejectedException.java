package com.OnlineBankingSystem.transaction;

public class TransactionRejectedException extends RuntimeException {

	public TransactionRejectedException(String message) {
		super(message);
	}
}
