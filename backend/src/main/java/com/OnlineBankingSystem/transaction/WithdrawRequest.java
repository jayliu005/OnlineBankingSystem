package com.OnlineBankingSystem.transaction;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WithdrawRequest(
		@NotNull(message = "Please choose a valid account")
		Long accountId,

		@NotNull(message = "Amount is required")
		@DecimalMin(value = "0.01", message = "Withdraw amount must be at least 0.01")
		@DecimalMax(value = "2000.00", message = "Withdraw amount must not exceed 2000.00")
		BigDecimal amount,

		@NotBlank(message = "Security Pin is required")
		String securityPin) {
}
