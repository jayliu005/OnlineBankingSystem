package com.OnlineBankingSystem.user;

import java.security.Principal;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

	private final UserProfileService profileService;

	public UserProfileController(UserProfileService profileService) {
		this.profileService = profileService;
	}

	@GetMapping
	public UserProfileResponse getProfile(Principal principal) {
		return profileService.getProfile(principal.getName());
	}

	@PutMapping
	public UserProfileResponse updateProfile(
			Principal principal, @Valid @RequestBody UpdateUserProfileRequest request) {
		return profileService.updateProfile(principal.getName(), request);
	}
}
