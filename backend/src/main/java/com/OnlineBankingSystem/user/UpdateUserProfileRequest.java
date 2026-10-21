package com.OnlineBankingSystem.user;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
		@NotBlank @Size(min = 2, max = 50) String firstName,
		@NotBlank @Size(min = 2, max = 50) String lastName,
		@Size(max = 1) String middleInitial,
		@NotNull @Pattern(regexp = "[MF]", message = "Please select your gender!") String gender,
		@NotNull @Past LocalDate dateOfBirth,
		@NotBlank @Size(min = 2, max = 100) String street,
		@NotBlank @Size(min = 2, max = 40) String city,
		@NotBlank @Size(min = 2, max = 40) String state,
		@NotBlank @Pattern(
				regexp = "^\\d{5}(?:[-\\s]\\d{4})?$",
				message = "Please provide a valid zip code!") String zip,
		@NotBlank @Pattern(
				regexp = "^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$",
				message = "Please provide a valid phone number!") String phone,
		@NotBlank @Email(message = "Please provide a valid email address!") @Size(max = 80)
		String email) {

	public String normalizedMiddleInitial() {
		return middleInitial == null || middleInitial.isBlank() ? null : middleInitial;
	}
}
