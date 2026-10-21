package com.OnlineBankingSystem.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.OnlineBankingSystem.account.BankAccount;
import com.OnlineBankingSystem.account.BankAccountRepository;
import com.OnlineBankingSystem.user.BankUser;

@ExtendWith(MockitoExtension.class)
class MoneyTransactionServiceTests {

	@Mock
	private BankAccountRepository accountRepository;

	@Mock
	private TransactionRecordRepository transactionRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	private MoneyTransactionService service;
	private BankAccount account;
	private BankAccount destinationAccount;

	@BeforeEach
	void setUp() {
		var bankUser = new BankUser(
				"transaction-owner",
				"password",
				"Transaction",
				"Owner",
				null,
				"F",
				LocalDate.of(1990, 1, 1),
				"1 Main Street",
				"Taipei",
				"Taiwan",
				"10001",
				"123-456-7890",
				"transaction.owner@example.com");
		account = new BankAccount(
				"Checking",
				new BigDecimal("100.00"),
				"hashed-pin",
				LocalDateTime.now(),
				bankUser);
		destinationAccount = new BankAccount(
				"Saving",
				new BigDecimal("20.00"),
				"destination-hashed-pin",
				LocalDateTime.now(),
				bankUser);
		service = new MoneyTransactionService(
				accountRepository, transactionRepository, passwordEncoder);
	}

	@Test
	void depositUpdatesBalanceAndCreatesRecord() {
		when(accountRepository.findByIdAndBankUserUserName(10L, "transaction-owner"))
				.thenReturn(Optional.of(account));
		when(passwordEncoder.matches("1234", "hashed-pin")).thenReturn(true);
		when(transactionRepository.save(any(TransactionRecord.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.deposit(
				"transaction-owner", new DepositRequest(10L, new BigDecimal("25.50"), "1234"));

		assertEquals(new BigDecimal("125.50"), account.getAccountBalance());
		var captor = ArgumentCaptor.forClass(TransactionRecord.class);
		verify(transactionRepository).save(captor.capture());
		assertEquals("Deposit", captor.getValue().getTransactionType());
		assertEquals(new BigDecimal("25.50"), captor.getValue().getTransactionAmount());
	}

	@Test
	void withdrawUpdatesBalanceAndCreatesRecord() {
		when(accountRepository.findByIdAndBankUserUserName(10L, "transaction-owner"))
				.thenReturn(Optional.of(account));
		when(passwordEncoder.matches("1234", "hashed-pin")).thenReturn(true);
		when(transactionRepository.save(any(TransactionRecord.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.withdraw(
				"transaction-owner", new WithdrawRequest(10L, new BigDecimal("40.00"), "1234"));

		assertEquals(new BigDecimal("60.00"), account.getAccountBalance());
		var captor = ArgumentCaptor.forClass(TransactionRecord.class);
		verify(transactionRepository).save(captor.capture());
		assertEquals("Withdraw", captor.getValue().getTransactionType());
	}

	@Test
	void withdrawRejectsAmountAboveBalanceWithoutChangingData() {
		when(accountRepository.findByIdAndBankUserUserName(10L, "transaction-owner"))
				.thenReturn(Optional.of(account));
		when(passwordEncoder.matches("1234", "hashed-pin")).thenReturn(true);

		assertThrows(
				TransactionRejectedException.class,
				() -> service.withdraw(
						"transaction-owner",
						new WithdrawRequest(10L, new BigDecimal("100.01"), "1234")));

		assertEquals(new BigDecimal("100.00"), account.getAccountBalance());
		verify(transactionRepository, never()).save(any());
	}

	@Test
	void transactionRejectsIncorrectPinWithoutChangingData() {
		when(accountRepository.findByIdAndBankUserUserName(10L, "transaction-owner"))
				.thenReturn(Optional.of(account));
		when(passwordEncoder.matches("9999", "hashed-pin")).thenReturn(false);

		assertThrows(
				TransactionRejectedException.class,
				() -> service.deposit(
						"transaction-owner",
						new DepositRequest(10L, new BigDecimal("25.00"), "9999")));

		assertEquals(new BigDecimal("100.00"), account.getAccountBalance());
		verify(transactionRepository, never()).save(any());
	}

	@Test
	void transferMovesBalanceBetweenAccountsAndCreatesRecord() {
		when(accountRepository.findByIdAndBankUserUserName(10L, "transaction-owner"))
				.thenReturn(Optional.of(account));
		when(accountRepository.findById(20L)).thenReturn(Optional.of(destinationAccount));
		when(passwordEncoder.matches("1234", "hashed-pin")).thenReturn(true);
		when(transactionRepository.save(any(TransactionRecord.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.transfer(
				"transaction-owner",
				new TransferRequest(10L, 20L, new BigDecimal("35.25"), "1234"));

		assertEquals(new BigDecimal("64.75"), account.getAccountBalance());
		assertEquals(new BigDecimal("55.25"), destinationAccount.getAccountBalance());
		var captor = ArgumentCaptor.forClass(TransactionRecord.class);
		verify(transactionRepository).save(captor.capture());
		assertEquals("Transfer", captor.getValue().getTransactionType());
		assertEquals(account, captor.getValue().getAccountFrom());
		assertEquals(destinationAccount, captor.getValue().getAccountTo());
	}

	@Test
	void transferRejectsMissingDestinationWithoutChangingData() {
		when(accountRepository.findByIdAndBankUserUserName(10L, "transaction-owner"))
				.thenReturn(Optional.of(account));
		when(accountRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(
				AccountNotFoundException.class,
				() -> service.transfer(
						"transaction-owner",
						new TransferRequest(10L, 99L, new BigDecimal("10.00"), "1234")));

		assertEquals(new BigDecimal("100.00"), account.getAccountBalance());
		verify(transactionRepository, never()).save(any());
	}

	@Test
	void transferRejectsSameAccountWithoutChangingData() {
		when(accountRepository.findByIdAndBankUserUserName(10L, "transaction-owner"))
				.thenReturn(Optional.of(account));
		when(accountRepository.findById(10L)).thenReturn(Optional.of(account));

		assertThrows(
				TransactionRejectedException.class,
				() -> service.transfer(
						"transaction-owner",
						new TransferRequest(10L, 10L, new BigDecimal("10.00"), "1234")));

		assertEquals(new BigDecimal("100.00"), account.getAccountBalance());
		verify(transactionRepository, never()).save(any());
	}

	@Test
	void transferRejectsAmountAboveSourceBalanceWithoutChangingEitherAccount() {
		when(accountRepository.findByIdAndBankUserUserName(10L, "transaction-owner"))
				.thenReturn(Optional.of(account));
		when(accountRepository.findById(20L)).thenReturn(Optional.of(destinationAccount));
		when(passwordEncoder.matches("1234", "hashed-pin")).thenReturn(true);

		assertThrows(
				TransactionRejectedException.class,
				() -> service.transfer(
						"transaction-owner",
						new TransferRequest(10L, 20L, new BigDecimal("100.01"), "1234")));

		assertEquals(new BigDecimal("100.00"), account.getAccountBalance());
		assertEquals(new BigDecimal("20.00"), destinationAccount.getAccountBalance());
		verify(transactionRepository, never()).save(any());
	}
}
