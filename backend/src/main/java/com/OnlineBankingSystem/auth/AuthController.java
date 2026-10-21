package com.OnlineBankingSystem.auth;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final HttpSessionSecurityContextRepository securityContextRepository =
			new HttpSessionSecurityContextRepository();

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthUserResponse> register(
			@Valid @RequestBody RegisterRequest request,
			HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		var bankUser = authService.register(request);
		saveAuthentication(
				authService.registrationAuthentication(bankUser), servletRequest, servletResponse);
		return ResponseEntity.status(HttpStatus.CREATED).body(AuthUserResponse.from(bankUser));
	}

	@PostMapping("/login")
	public AuthUserResponse login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		var authentication = authService.authenticate(request);
		saveAuthentication(authentication, servletRequest, servletResponse);
		return AuthUserResponse.from(authService.requireUser(authentication.getName()));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		SecurityContextHolder.clearContext();
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/session")
	public ResponseEntity<AuthUserResponse> session(Principal principal) {
		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(AuthUserResponse.from(authService.requireUser(principal.getName())));
	}

	@GetMapping("/username-availability")
	public UsernameAvailabilityResponse usernameAvailability(
			@RequestParam @NotBlank @Size(min = 2, max = 30) String userName) {
		return authService.checkAvailability(userName);
	}

	private void saveAuthentication(
			Authentication authentication,
			HttpServletRequest request,
			HttpServletResponse response) {
		var context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, request, response);
	}
}
