package com.OnlineBankingSystem.user;

import java.time.LocalDate;

public record UserProfileResponse(
		Long id,
		String userName,
		String firstName,
		String lastName,
		String middleInitial,
		String gender,
		LocalDate dateOfBirth,
		String street,
		String city,
		String state,
		String zip,
		String phone,
		String email) {

	static UserProfileResponse from(BankUser user) {
		return new UserProfileResponse(
				user.getId(), user.getUserName(), user.getFirstName(), user.getLastName(),
				user.getMiddleInitial(), user.getGender(), user.getDateOfBirth(), user.getStreet(),
				user.getCity(), user.getState(), user.getZip(), user.getPhone(), user.getEmail());
	}
}
