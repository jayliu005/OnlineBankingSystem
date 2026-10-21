package com.OnlineBankingSystem.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.OnlineBankingSystem.account.BankAccount;
import com.OnlineBankingSystem.account.BankAccountRepository;

@Service
public class MoneyTransactionService {

	private final BankAccountRepository accountRepository;
	private final TransactionRecordRepository transactionRepository;
	private final PasswordEncoder passwordEncoder;

	public MoneyTransactionService(
			BankAccountRepository accountRepository,
			TransactionRecordRepository transactionRepository,
			PasswordEncoder passwordEncoder) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public TransactionResponse deposit(String userName, DepositRequest request) {
		BankAccount account = requireOwnedAccount(request.accountId(), userName);
		verifyPin(request.securityPin(), account);
		account.deposit(request.amount());
		return saveTransaction(account, "Deposit", request.amount());
	}

	@Transactional
	public TransactionResponse withdraw(String userName, WithdrawRequest request) {
		BankAccount account = requireOwnedAccount(request.accountId(), userName);
		verifyPin(request.securityPin(), account);
		if (request.amount().compareTo(account.getAccountBalance()) > 0) {
			throw new TransactionRejectedException(
					"No enough money in account ID '%s'".formatted(account.getId()));
		}
		account.withdraw(request.amount());
		return saveTransaction(account, "Withdraw", request.amount());
	}

	@Transactional
	public TransactionResponse transfer(String userName, TransferRequest request) {
		BankAccount accountFrom = requireOwnedAccount(request.accountFromId(), userName);
		BankAccount accountTo = accountRepository.findById(request.accountToId())
				.orElseThrow(() -> new AccountNotFoundException(request.accountToId()));
		if (request.accountFromId().equals(request.accountToId())) {
			throw new TransactionRejectedException(
					"Cannot transfer money within the same account!");
		}
		verifyPin(request.securityPin(), accountFrom);
		if (request.amount().compareTo(accountFrom.getAccountBalance()) > 0) {
			throw new TransactionRejectedException(
					"No enough money in account ID '%s'".formatted(request.accountFromId()));
		}

		accountFrom.withdraw(request.amount());
		accountTo.deposit(request.amount());
		return saveTransaction(accountFrom, accountTo, "Transfer", request.amount());
	}

	@Transactional(readOnly = true)
	public List<TransactionHistoryResponse> history(
			String userName, Long accountId, LocalDate startDate, LocalDate endDate) {
		BankAccount account = requireOwnedAccount(accountId, userName);
		if (startDate.isAfter(endDate)) {
			throw new TransactionRejectedException("Start date must not be after end date");
		}
		LocalDateTime startTime = startDate.atStartOfDay();
		LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();
		return transactionRepository.findForAccountAndTimeRange(account.getId(), startTime, endTime)
				.stream()
				.map(record -> TransactionHistoryResponse.from(record, account.getId()))
				.toList();
	}

	private BankAccount requireOwnedAccount(Long accountId, String userName) {
		return accountRepository.findByIdAndBankUserUserName(accountId, userName)
				.orElseThrow(() -> new AccountNotFoundException(accountId));
	}

	private void verifyPin(String securityPin, BankAccount account) {
		if (!passwordEncoder.matches(securityPin, account.getAccountPin())) {
			throw new TransactionRejectedException("Incorrect account security pin");
		}
	}

	private TransactionResponse saveTransaction(
			BankAccount account, String transactionType, BigDecimal amount) {
		return saveTransaction(account, null, transactionType, amount);
	}

	private TransactionResponse saveTransaction(
			BankAccount accountFrom,
			BankAccount accountTo,
			String transactionType,
			BigDecimal amount) {
		var record = new TransactionRecord(
				accountFrom, accountTo, transactionType, amount, LocalDateTime.now());
		return TransactionResponse.from(transactionRepository.save(record));
	}
}
