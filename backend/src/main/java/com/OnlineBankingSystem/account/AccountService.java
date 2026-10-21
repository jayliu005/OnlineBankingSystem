package com.OnlineBankingSystem.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.OnlineBankingSystem.user.BankUser;
import com.OnlineBankingSystem.user.BankUserRepository;

@Service
public class AccountService {

	private final BankAccountRepository accountRepository;
	private final BankUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AccountService(
			BankAccountRepository accountRepository,
			BankUserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.accountRepository = accountRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<AccountResponse> listAccounts(String userName) {
		BankUser bankUser = requireUser(userName);
		return accountRepository.findAllByBankUserIdOrderByIdAsc(bankUser.getId()).stream()
				.map(AccountResponse::from)
				.toList();
	}

	@Transactional
	public AccountResponse createAccount(String userName, CreateAccountRequest request) {
		BankUser bankUser = requireUser(userName);
		var account = new BankAccount(
				request.accountType(),
				BigDecimal.ZERO,
				passwordEncoder.encode(request.securityPin()),
				LocalDateTime.now(),
				bankUser);
		return AccountResponse.from(accountRepository.save(account));
	}

	@Transactional(readOnly = true)
	public AccountExistenceResponse checkExistence(Long accountId) {
		boolean exists = accountRepository.existsById(accountId);
		String message = exists
				? "Account with id '%s' exists".formatted(accountId)
				: "No such account with account id '%s'".formatted(accountId);
		return new AccountExistenceResponse(exists, message);
	}

	private BankUser requireUser(String userName) {
		return userRepository.findByUserName(userName)
				.orElseThrow(() -> new UsernameNotFoundException(
						"No such user with userName '%s'".formatted(userName)));
	}
}
