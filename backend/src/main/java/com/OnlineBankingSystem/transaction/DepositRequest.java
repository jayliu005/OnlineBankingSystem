package com.OnlineBankingSystem.transaction;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepositRequest(
		@NotNull(message = "Please choose a valid account")
		Long accountId,

		@NotNull(message = "Amount is required")
		@DecimalMin(value = "0.01", message = "Deposit amount must be at least 0.01")
		@DecimalMax(value = "50000.00", message = "Deposit amount must not exceed 50000.00")
		BigDecimal amount,

		@NotBlank(message = "Security Pin is required")
		String securityPin) {
}
