package com.OnlineBankingSystem.transaction;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(
		@NotNull(message = "Please choose a valid account")
		Long accountFromId,

		@NotNull(message = "To Account is required")
		Long accountToId,

		@NotNull(message = "Amount is required")
		@DecimalMin(value = "0.01", message = "Transfer amount must be at least 0.01")
		@DecimalMax(value = "10000.00", message = "Transfer amount must not exceed 10000.00")
		BigDecimal amount,

		@NotBlank(message = "Security Pin is required")
		String securityPin) {
}
