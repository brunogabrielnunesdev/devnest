package com.devnest.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.dto.AuthResponse;
import com.devnest.auth.dto.LoginRequest;
import com.devnest.auth.dto.RegisterRequest;
import com.devnest.shared.exception.ConflictException;
import com.devnest.user.domain.UserRole;
import com.devnest.user.domain.UserStatus;
import com.devnest.user.repository.UserRepository;
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
		AuthResponse response = authService.register(new RegisterRequest(
			"Bruno@Example.com",
			"strong-password",
			"Bruno"
		));

		assertThat(response.accessToken()).isNotBlank();
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.email()).isEqualTo("bruno@example.com");
		assertThat(response.role()).isEqualTo(UserRole.STUDENT);

		var savedUser = userRepository.findByEmail("bruno@example.com").orElseThrow();
		assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(savedUser.getProfile()).isNotNull();
		assertThat(savedUser.getProfile().getDisplayName()).isEqualTo("Bruno");
		assertThat(savedUser.getPasswordHash()).isNotEqualTo("strong-password");
	}

	@Test
	void registerRejectsDuplicateEmail() {
		authService.register(new RegisterRequest("duplicate@example.com", "strong-password", "Bruno"));

		assertThatThrownBy(() -> authService.register(
			new RegisterRequest("DUPLICATE@example.com", "another-password", "Other")
		)).isInstanceOf(ConflictException.class);
	}

	@Test
	void loginRejectsInvalidPassword() {
		authService.register(new RegisterRequest("login@example.com", "strong-password", "Bruno"));

		assertThatThrownBy(() -> authService.login(new LoginRequest("login@example.com", "wrong-password")))
			.isInstanceOf(BadCredentialsException.class);
	}

	@Test
	void loginRejectsBlockedUser() {
		authService.register(new RegisterRequest("blocked@example.com", "strong-password", "Bruno"));
		var user = userRepository.findByEmail("blocked@example.com").orElseThrow();
		user.setStatus(UserStatus.BLOCKED);
		userRepository.save(user);

		assertThatThrownBy(() -> authService.login(new LoginRequest("blocked@example.com", "strong-password")))
			.isInstanceOf(BadCredentialsException.class);
	}
}
