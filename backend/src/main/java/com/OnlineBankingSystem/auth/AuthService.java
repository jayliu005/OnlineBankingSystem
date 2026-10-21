package com.OnlineBankingSystem.auth;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.OnlineBankingSystem.user.BankUser;
import com.OnlineBankingSystem.user.BankUserRepository;

@Service
public class AuthService {

	private final BankUserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;

	public AuthService(
			BankUserRepository repository,
			PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
	}

	@Transactional
	public BankUser register(RegisterRequest request) {
		if (repository.existsByUserName(request.userName())) {
			throw new UsernameAlreadyExistsException(request.userName());
		}

		var bankUser = new BankUser(
				request.userName(),
				passwordEncoder.encode(request.password()),
				request.firstName(),
				request.lastName(),
				emptyToNull(request.middleInitial()),
				request.gender(),
				request.dateOfBirth(),
				request.street(),
				request.city(),
				request.state(),
				request.zip(),
				request.phone(),
				request.email());

		return repository.save(bankUser);
	}

	public Authentication authenticate(LoginRequest request) {
		if (!repository.existsByUserName(request.userName())) {
			throw new UsernameNotFoundException(
					"No such user with userName '%s'".formatted(request.userName()));
		}

		return authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(
						request.userName(), request.password()));
	}

	public Authentication registrationAuthentication(BankUser bankUser) {
		return UsernamePasswordAuthenticationToken.authenticated(
				bankUser.getUserName(),
				null,
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
	}

	@Transactional(readOnly = true)
	public BankUser requireUser(String userName) {
		return repository.findByUserName(userName)
				.orElseThrow(() -> new UsernameNotFoundException(
						"No such user with userName '%s'".formatted(userName)));
	}

	@Transactional(readOnly = true)
	public UsernameAvailabilityResponse checkAvailability(String userName) {
		boolean available = !repository.existsByUserName(userName);
		String message = available
				? "User Name '%s' is available".formatted(userName)
				: "User Name '%s' has been used".formatted(userName);
		return new UsernameAvailabilityResponse(available, message);
	}

	private String emptyToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}
}
