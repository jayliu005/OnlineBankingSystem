package com.OnlineBankingSystem.account;

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

import com.OnlineBankingSystem.user.BankUser;

@Entity
@Table(name = "ACCOUNT")
public class BankAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernateSequence")
	@SequenceGenerator(
			name = "hibernateSequence",
			sequenceName = "HIBERNATE_SEQUENCE",
			allocationSize = 1)
	@Column(name = "ACCOUNT_ID", nullable = false)
	private Long id;

	@Column(name = "ACCOUNT_TYPE", nullable = false, length = 10)
	private String accountType;

	@Column(name = "ACCOUNT_BALANCE", nullable = false)
	private BigDecimal accountBalance;

	@Column(name = "ACCOUNT_PIN", nullable = false, length = 60)
	private String accountPin;

	@Column(name = "DATE_OF_CREATED", nullable = false)
	private LocalDateTime dateOfCreated;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "USER_ID", nullable = false)
	private BankUser bankUser;

	protected BankAccount() {
	}

	public BankAccount(
			String accountType,
			BigDecimal accountBalance,
			String accountPin,
			LocalDateTime dateOfCreated,
			BankUser bankUser) {
		this.accountType = accountType;
		this.accountBalance = accountBalance;
		this.accountPin = accountPin;
		this.dateOfCreated = dateOfCreated;
		this.bankUser = bankUser;
	}

	public Long getId() {
		return id;
	}

	public String getAccountType() {
		return accountType;
	}

	public BigDecimal getAccountBalance() {
		return accountBalance;
	}

	public String getAccountPin() {
		return accountPin;
	}

	public LocalDateTime getDateOfCreated() {
		return dateOfCreated;
	}

	public BankUser getBankUser() {
		return bankUser;
	}

	public void deposit(BigDecimal amount) {
		accountBalance = accountBalance.add(amount);
	}

	public void withdraw(BigDecimal amount) {
		accountBalance = accountBalance.subtract(amount);
	}
}
