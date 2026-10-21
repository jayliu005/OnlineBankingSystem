package com.OnlineBankingSystem.auth;

public class UsernameAlreadyExistsException extends RuntimeException {

	public UsernameAlreadyExistsException(String userName) {
		super("User Name '%s' has been used".formatted(userName));
	}
}
