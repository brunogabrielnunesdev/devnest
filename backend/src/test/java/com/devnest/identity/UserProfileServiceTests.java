package com.devnest.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.service.auth.AuthService;
import com.devnest.auth.dto.login.LoginRequest;
import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.common.exception.ConflictException;
import com.devnest.common.exception.UnauthorizedException;
import com.devnest.identity.entity.User;
import com.devnest.identity.repository.UserRepository;
import com.devnest.profile.service.UserProfileService;
import com.devnest.profile.dto.ChangePasswordRequest;
import com.devnest.profile.dto.UserProfileUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class UserProfileServiceTests {

	@Autowired
	private UserProfileService userProfileService;

	@Autowired
	private AuthService authService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void authenticatedUserCanUpdateOwnProfile() {
		User user = saveStudent("profile-update@example.com", "OldPassword123!");
		authenticate(user);

		var response = userProfileService.updateMyProfile(new UserProfileUpdateRequest(
			"  Bruno Dev  ",
			" Bruno Andrade ",
			" Backend builder ",
			"https://cdn.example.com/avatar.png",
			"https://github.com/bruno",
			"https://linkedin.com/in/bruno",
			"https://bruno.dev",
			" Sao Paulo "
		));

		assertThat(response.displayName()).isEqualTo("Bruno Dev");
		assertThat(response.fullName()).isEqualTo("Bruno Andrade");
		assertThat(response.bio()).isEqualTo("Backend builder");
		assertThat(response.location()).isEqualTo("Sao Paulo");
	}

	@Test
	void userCanChangePasswordAndLoginWithNewPassword() {
		User user = saveStudent("password-change@example.com", "OldPassword123!");
		authenticate(user);

		var authResponseBeforePasswordChange = authService.login(new LoginRequest(
			"password-change@example.com",
			"OldPassword123!"
		));

		userProfileService.changeMyPassword(new ChangePasswordRequest(
			"OldPassword123!",
			"NewPassword123!"
		));

		var authResponse = authService.login(new LoginRequest("password-change@example.com", "NewPassword123!"));

		assertThat(authResponse.accessToken()).isNotBlank();
		assertThat(passwordEncoder.matches("NewPassword123!", reload(user).getPasswordHash())).isTrue();
		assertThat(reload(user).getTokenVersion()).isEqualTo(1);
		assertThatThrownBy(() -> authService.refreshToken(authResponseBeforePasswordChange.refreshToken()))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("Invalid refresh token.");
	}

	@Test
	void userCannotReuseCurrentPassword() {
		User user = saveStudent("password-same@example.com", "OldPassword123!");
		authenticate(user);

		assertThatThrownBy(() -> userProfileService.changeMyPassword(new ChangePasswordRequest(
			"OldPassword123!",
			"OldPassword123!"
		))).isInstanceOf(ConflictException.class)
			.hasMessage("New password must be different from the current password.");
	}

	@Test
	void userCannotChangePasswordWithWrongCurrentPassword() {
		User user = saveStudent("password-wrong-current@example.com", "OldPassword123!");
		authenticate(user);

		assertThatThrownBy(() -> userProfileService.changeMyPassword(new ChangePasswordRequest(
			"WrongPassword123!",
			"NewPassword123!"
		))).isInstanceOf(BadCredentialsException.class)
			.hasMessage("Current password is invalid.");
	}

	private void authenticate(User user) {
		CustomAuthentication customAuthentication = new CustomAuthentication(user);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			customAuthentication,
			null,
			customAuthentication.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private User saveStudent(String email, String rawPassword) {
		return userRepository.save(User.createStudent(email, passwordEncoder.encode(rawPassword), "Student"));
	}

	private User reload(User user) {
		return userRepository.findById(user.getId()).orElseThrow();
	}
}

