package com.OnlineBankingSystem.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import com.OnlineBankingSystem.account.BankAccount;

@Entity
@Table(name = "TRANSACTION_REC")
public class TransactionRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernateSequence")
	@SequenceGenerator(
			name = "hibernateSequence",
			sequenceName = "HIBERNATE_SEQUENCE",
			allocationSize = 1)
	@Column(name = "TRAN_ID", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ACCOUNT_ID_FROM", nullable = false)
	private BankAccount accountFrom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ACCOUNT_ID_TO")
	private BankAccount accountTo;

	@Column(name = "TRANSACTION_TYPE", nullable = false, length = 10)
	private String transactionType;

	@Column(name = "TRANSACTION_AMOUNT", nullable = false)
	private BigDecimal transactionAmount;

	@Column(name = "TRANSACTION_TIME", nullable = false)
	private LocalDateTime transactionTime;

	protected TransactionRecord() {
	}

	public TransactionRecord(
			BankAccount accountFrom,
			BankAccount accountTo,
			String transactionType,
			BigDecimal transactionAmount,
			LocalDateTime transactionTime) {
		this.accountFrom = accountFrom;
		this.accountTo = accountTo;
		this.transactionType = transactionType;
		this.transactionAmount = transactionAmount;
		this.transactionTime = transactionTime;
	}

	public Long getId() {
		return id;
	}

	public BankAccount getAccountFrom() {
		return accountFrom;
	}

	public BankAccount getAccountTo() {
		return accountTo;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public LocalDateTime getTransactionTime() {
		return transactionTime;
	}
}
