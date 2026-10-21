package com.OnlineBankingSystem.auth;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 30) String userName,
        @NotBlank @Size(min = 3) String password,
        @NotBlank String repeatPassword,
        @NotBlank @Size(min = 2, max = 50) String firstName,
        @NotBlank @Size(min = 2, max = 50) String lastName,
        @Size(max = 1) String middleInitial,
        @NotNull @Pattern(regexp = "[MF]", message = "Please select your gender!") String gender,
        @NotNull @Past LocalDate dateOfBirth,
        @NotBlank @Size(min = 2, max = 100) String street,
        @NotBlank @Size(min = 2, max = 40) String city,
        @NotBlank @Size(min = 2, max = 40) String state,
        @NotBlank
        @Pattern(
                regexp = "^\\d{5}(?:[-\\s]\\d{4})?$",
                message = "Please provide a valid zip code!")
        String zip,
        @NotBlank
        @Pattern(
                regexp = "^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$",
                message = "Please provide a valid phone number!")
        String phone,
        @NotBlank @Email(message = "Please provide a valid email address!") @Size(max = 80)
        String email) {

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(repeatPassword);
    }
}
