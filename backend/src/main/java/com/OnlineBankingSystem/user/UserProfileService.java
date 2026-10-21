package com.OnlineBankingSystem.user;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

	private final BankUserRepository userRepository;

	public UserProfileService(BankUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getProfile(String userName) {
		return UserProfileResponse.from(requireUser(userName));
	}

	@Transactional
	public UserProfileResponse updateProfile(String userName, UpdateUserProfileRequest request) {
		BankUser user = requireUser(userName);
		user.updateProfile(
				request.firstName(), request.lastName(), request.normalizedMiddleInitial(), request.gender(),
				request.dateOfBirth(), request.street(), request.city(), request.state(), request.zip(),
				request.phone(), request.email());
		return UserProfileResponse.from(userRepository.save(user));
	}

	private BankUser requireUser(String userName) {
		return userRepository.findByUserName(userName)
				.orElseThrow(() -> new UsernameNotFoundException(
						"No such user with userName '%s'".formatted(userName)));
	}
}
