package com.devnest.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.dto.refreshtoken.RefreshTokenResponse;
import com.devnest.auth.service.auth.AuthService;
import com.devnest.auth.dto.login.responselogin.AuthResponse;
import com.devnest.auth.dto.login.LoginRequest;
import com.devnest.auth.dto.register.RegisterRequest;
import com.devnest.common.exception.ConflictException;
import com.devnest.identity.entity.UserStatus;
import com.devnest.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;

@SpringBootTest
class AuthServiceTests {

	@Autowired
	private AuthService authService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void registerCreatesActiveStudentWithProfileAndToken() {
		AuthResponse response = authService.registerUser(new RegisterRequest(
			"Bruno@Example.com",
			"strong-password",
			"Bruno"
		));

		assertThat(response.accessToken()).isNotBlank();
		assertThat(response.refreshToken()).isNotBlank();

		var savedUser = userRepository.findByEmail("bruno@example.com").orElseThrow();
		assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(savedUser.getProfile()).isNotNull();
		assertThat(savedUser.getProfile().getDisplayName()).isEqualTo("Bruno");
		assertThat(savedUser.getPasswordHash()).isNotEqualTo("strong-password");
		assertThat(savedUser.getTokenVersion()).isZero();
	}

	@Test
	void registerRejectsDuplicateEmail() {
		authService.registerUser(new RegisterRequest("duplicate@example.com", "strong-password", "Bruno"));

		assertThatThrownBy(() -> authService.registerUser(
			new RegisterRequest("DUPLICATE@example.com", "another-password", "Other")
		)).isInstanceOf(ConflictException.class)
			.hasMessage("Unable to complete registration.");
	}

	@Test
	void loginRejectsInvalidPassword() {
		authService.registerUser(new RegisterRequest("login@example.com", "strong-password", "Bruno"));

		assertThatThrownBy(() -> authService.login(new LoginRequest("login@example.com", "wrong-password")))
			.isInstanceOf(BadCredentialsException.class);
	}

	@Test
	void loginRejectsBlockedUser() {
		authService.registerUser(new RegisterRequest("blocked@example.com", "strong-password", "Bruno"));
		var user = userRepository.findByEmail("blocked@example.com").orElseThrow();
		user.setStatus(UserStatus.BLOCKED);
		userRepository.save(user);

		assertThatThrownBy(() -> authService.login(new LoginRequest("blocked@example.com", "strong-password")))
			.isInstanceOf(BadCredentialsException.class);
	}

	@Test
	void refreshRotatesTokensAndInvalidatesPreviousRefreshToken() {
		AuthResponse authResponse = authService.registerUser(new RegisterRequest(
			"rotate@example.com",
			"strong-password",
			"Bruno"
		));

		RefreshTokenResponse refreshResponse = authService.refreshToken(authResponse.refreshToken());

		assertThat(refreshResponse.accessToken()).isNotBlank();
		assertThat(refreshResponse.refreshToken()).isNotBlank();
		assertThat(refreshResponse.refreshToken()).isNotEqualTo(authResponse.refreshToken());
		assertThat(userRepository.findByEmail("rotate@example.com").orElseThrow().getTokenVersion()).isEqualTo(1);

		assertThatThrownBy(() -> authService.refreshToken(authResponse.refreshToken()))
			.isInstanceOf(com.devnest.common.exception.UnauthorizedException.class)
			.hasMessage("Invalid refresh token.");
	}
}

