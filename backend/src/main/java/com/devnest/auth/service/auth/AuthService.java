package com.devnest.auth.service.auth;

import com.devnest.auth.dto.login.LoginRequest;
import com.devnest.auth.dto.login.responselogin.AuthResponse;
import com.devnest.auth.dto.refreshtoken.RefreshTokenResponse;
import com.devnest.auth.dto.register.RegisterRequest;
import com.devnest.auth.service.token.TokenService;
import com.devnest.common.exception.ConflictException;
import com.devnest.common.exception.UnauthorizedException;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserStatus;
import com.devnest.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final String GENERIC_REGISTRATION_FAILURE_MESSAGE = "Unable to complete registration.";

	private final TokenService tokenService;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	@Transactional
	public AuthResponse registerUser(RegisterRequest request) {

		String email = normalizeEmail(request.email());

		if (userRepository.existsByEmail(email)) {
			// Burn comparable work on the duplicate path so the public endpoint leaks less information.
			passwordEncoder.encode(request.password());
			throw new ConflictException(GENERIC_REGISTRATION_FAILURE_MESSAGE);
		}

		User user = User.createStudent(
			email,
			passwordEncoder.encode(request.password()),
			request.displayName().trim()
		);

		User savedUser = userRepository.save(user);

		return generateTokens(savedUser);
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

		return generateTokens(user);
	}

	@Transactional
	public RefreshTokenResponse refreshToken(String refreshToken) {
		if (!tokenService.isRefreshToken(refreshToken)) {
			throw new UnauthorizedException("Invalid refresh token.");
		}

		String subject = tokenService.getSubject(refreshToken);
		if (subject == null) {
			throw new UnauthorizedException("Invalid refresh token.");
		}

		UUID userId;
		try {
			userId = UUID.fromString(subject);
		} catch (IllegalArgumentException exception) {
			throw new UnauthorizedException("Invalid refresh token.");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new UnauthorizedException("Invalid refresh token.");
		}

		Integer tokenVersion = tokenService.getTokenVersion(refreshToken);
		if (tokenVersion == null || !tokenVersion.equals(user.getTokenVersion())) {
			throw new UnauthorizedException("Invalid refresh token.");
		}

		user.setTokenVersion(user.getTokenVersion() + 1);
		String accessToken = tokenService.generateAccessToken(user);
		String newRefreshToken = tokenService.generateRefreshToken(user);
		return new RefreshTokenResponse(accessToken, newRefreshToken);
	}
	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private AuthResponse generateTokens(User user){
		String accessToken = tokenService.generateAccessToken(user);
		String refreshToken = tokenService.generateRefreshToken(user);
		return new AuthResponse(accessToken, refreshToken);
	}

}
