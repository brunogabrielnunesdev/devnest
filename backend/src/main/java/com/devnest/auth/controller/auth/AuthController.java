package com.devnest.auth.controller.auth;

import com.devnest.auth.dto.refreshtoken.RefreshTokenRequest;
import com.devnest.auth.dto.refreshtoken.RefreshTokenResponse;
import com.devnest.auth.service.auth.AuthService;
import com.devnest.auth.dto.login.responselogin.AuthResponse;
import com.devnest.auth.dto.login.LoginRequest;
import com.devnest.auth.dto.register.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	@PreAuthorize("permitAll()")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		var response = authService.registerUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	@PreAuthorize("permitAll()")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		var response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/refresh")
	@PreAuthorize("permitAll()")
	public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		var response = authService.refreshToken(request.refreshToken());
		return ResponseEntity.ok(response);
	}


}

