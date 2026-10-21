package com.OnlineBankingSystem.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.OnlineBankingSystem.user.BankUser;
import com.OnlineBankingSystem.user.BankUserRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTests {

	@Mock
	private BankAccountRepository accountRepository;

	@Mock
	private BankUserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Test
	void createsAccountWithLegacyDefaultsAndHashedPin() {
		var bankUser = new BankUser(
				"account-owner",
				"password",
				"Account",
				"Owner",
				null,
				"M",
				LocalDate.of(1990, 1, 1),
				"1 Main Street",
				"Taipei",
				"Taiwan",
				"10001",
				"123-456-7890",
				"owner@example.com");
		when(userRepository.findByUserName("account-owner")).thenReturn(Optional.of(bankUser));
		when(passwordEncoder.encode("1234")).thenReturn("hashed-pin");
		when(accountRepository.save(any(BankAccount.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var service = new AccountService(accountRepository, userRepository, passwordEncoder);
		service.createAccount(
				"account-owner", new CreateAccountRequest("Checking", "1234", "1234"));

		var captor = ArgumentCaptor.forClass(BankAccount.class);
		verify(accountRepository).save(captor.capture());
		BankAccount savedAccount = captor.getValue();
		assertEquals("Checking", savedAccount.getAccountType());
		assertEquals(BigDecimal.ZERO, savedAccount.getAccountBalance());
		assertEquals("hashed-pin", savedAccount.getAccountPin());
		assertEquals(bankUser, savedAccount.getBankUser());
		assertNotNull(savedAccount.getDateOfCreated());
	}
}
