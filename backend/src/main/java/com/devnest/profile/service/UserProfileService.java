package com.devnest.profile.service;

import com.devnest.common.exception.ConflictException;
import com.devnest.auth.security.useridentity.CustomUserProvider;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserProfile;
import com.devnest.identity.repository.UserRepository;
import com.devnest.profile.dto.ChangePasswordRequest;
import com.devnest.profile.dto.UserProfileResponse;
import com.devnest.profile.dto.UserProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private final CustomUserProvider customUserProvider;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public UserProfileResponse getMyProfile() {
		return toResponse(getManagedAuthenticatedUser());
	}

	@Transactional
	public UserProfileResponse updateMyProfile(UserProfileUpdateRequest request) {
		User user = getManagedAuthenticatedUser();
		UserProfile profile = user.getProfile();
		String displayName = request.displayName().trim();

		if (displayName.isBlank()) {
			throw new ConflictException("Display name must not be blank.");
		}

		profile.setDisplayName(displayName);
		profile.setFullName(normalizeNullable(request.fullName()));
		profile.setBio(normalizeNullable(request.bio()));
		profile.setAvatarUrl(normalizeNullable(request.avatarUrl()));
		profile.setGithubUrl(normalizeNullable(request.githubUrl()));
		profile.setLinkedinUrl(normalizeNullable(request.linkedinUrl()));
		profile.setPortfolioUrl(normalizeNullable(request.portfolioUrl()));
		profile.setLocation(normalizeNullable(request.location()));

		return toResponse(user);
	}

	@Transactional
	public void changeMyPassword(ChangePasswordRequest request) {
		User user = getManagedAuthenticatedUser();

		if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
			throw new BadCredentialsException("Current password is invalid.");
		}

		if (request.currentPassword().equals(request.newPassword())) {
			throw new ConflictException("New password must be different from the current password.");
		}

		user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		user.setTokenVersion(user.getTokenVersion() + 1);
	}

	private User getManagedAuthenticatedUser() {
		User authenticatedUser = customUserProvider.getAuthenticatedUser();
		return userRepository.findById(authenticatedUser.getId())
			.orElseThrow(() -> new BadCredentialsException("Authenticated user was not found."));
	}

	private UserProfileResponse toResponse(User user) {
		UserProfile profile = user.getProfile();
		return new UserProfileResponse(
			user.getId(),
			user.getEmail(),
			user.getRole(),
			user.getStatus(),
			profile.getDisplayName(),
			profile.getFullName(),
			profile.getBio(),
			profile.getAvatarUrl(),
			profile.getGithubUrl(),
			profile.getLinkedinUrl(),
			profile.getPortfolioUrl(),
			profile.getLocation(),
			profile.getCreatedAt(),
			profile.getUpdatedAt()
		);
	}

	private String normalizeNullable(String value) {
		if (value == null) {
			return null;
		}

		String normalized = value.trim();
		return normalized.isBlank() ? null : normalized;
	}
}

