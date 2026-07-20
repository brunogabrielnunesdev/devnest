package com.devnest.profile.controller;

import com.devnest.profile.service.UserProfileService;
import com.devnest.profile.dto.ChangePasswordRequest;
import com.devnest.profile.dto.UserProfileResponse;
import com.devnest.profile.dto.UserProfileUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/perfil")
public class UserProfileController {

	private final UserProfileService userProfileService;

	@GetMapping
	public ResponseEntity<UserProfileResponse> getMyProfile() {
		var response = userProfileService.getMyProfile();
		return ResponseEntity.ok(response);
	}

	@PatchMapping
	public ResponseEntity<UserProfileResponse> updateMyProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
		var response = userProfileService.updateMyProfile(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/senha")
	public ResponseEntity<Void> changeMyPassword(@Valid @RequestBody ChangePasswordRequest request) {
		userProfileService.changeMyPassword(request);
		return ResponseEntity.noContent().build();
	}
}

