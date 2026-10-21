package com.OnlineBankingSystem.account;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping
	public List<AccountResponse> listAccounts(Principal principal) {
		return accountService.listAccounts(principal.getName());
	}

	@PostMapping
	public ResponseEntity<AccountResponse> createAccount(
			Principal principal, @Valid @RequestBody CreateAccountRequest request) {
		AccountResponse account = accountService.createAccount(principal.getName(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(account);
	}

	@GetMapping("/{accountId}/existence")
	public AccountExistenceResponse accountExistence(@PathVariable Long accountId) {
		return accountService.checkExistence(accountId);
	}
}
