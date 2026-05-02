package com.devnest.auth.service;

import com.devnest.auth.dto.AuthResponse;
import com.devnest.auth.dto.LoginRequest;
import com.devnest.auth.dto.RegisterRequest;
import com.devnest.auth.security.TokenService;
import com.devnest.shared.exception.ConflictException;
import com.devnest.user.domain.User;
import com.devnest.user.domain.UserStatus;
import com.devnest.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final TokenService tokenService;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.email());

		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email already registered.");
		}

		User user = User.createStudent(
			email,
			passwordEncoder.encode(request.password()),
			request.displayName().trim()
		);

		User savedUser = userRepository.save(user);
		return createAuthResponse(savedUser);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new BadCredentialsException("Invalid email or password.");
		}
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new BadCredentialsException("Invalid email or password.");
		}

		return createAuthResponse(user);
	}

	private AuthResponse createAuthResponse(User user) {
		String token = tokenService.generateToken(user);
		return AuthResponse.bearer(token, user.getId(), user.getEmail(), user.getRole());
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
