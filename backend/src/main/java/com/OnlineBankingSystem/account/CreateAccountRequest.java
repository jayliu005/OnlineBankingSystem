package com.OnlineBankingSystem.account;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateAccountRequest(
		@NotNull(message = "Account Type is required")
		@Pattern(regexp = "Checking|Saving", message = "Account Type must be Checking or Saving")
		String accountType,

		@NotBlank(message = "Security Pin is required")
		@Pattern(regexp = "\\d{4}", message = "Security Pin must be 4 digits")
		String securityPin,

		@NotBlank(message = "Repeat Security Pin is required")
		String repeatSecurityPin) {

	@JsonIgnore
	@AssertTrue(message = "Security Pins do not match")
	public boolean isPinConfirmed() {
		return securityPin == null || repeatSecurityPin == null
				|| securityPin.equals(repeatSecurityPin);
	}
}
