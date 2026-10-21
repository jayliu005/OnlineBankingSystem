package com.OnlineBankingSystem.transaction;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class MoneyTransactionController {

	private final MoneyTransactionService transactionService;

	public MoneyTransactionController(MoneyTransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping("/deposits")
	public ResponseEntity<TransactionResponse> deposit(
			Principal principal, @Valid @RequestBody DepositRequest request) {
		TransactionResponse transaction = transactionService.deposit(principal.getName(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
	}

	@PostMapping("/withdrawals")
	public ResponseEntity<TransactionResponse> withdraw(
			Principal principal, @Valid @RequestBody WithdrawRequest request) {
		TransactionResponse transaction = transactionService.withdraw(principal.getName(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
	}

	@PostMapping("/transfers")
	public ResponseEntity<TransactionResponse> transfer(
			Principal principal, @Valid @RequestBody TransferRequest request) {
		TransactionResponse transaction = transactionService.transfer(principal.getName(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
	}

	@GetMapping("/history")
	public List<TransactionHistoryResponse> history(
			Principal principal,
			@RequestParam Long accountId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return transactionService.history(principal.getName(), accountId, startDate, endDate);
	}
}
