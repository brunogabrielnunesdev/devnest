package com.devnest.auth.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
	@NotBlank(message = "")
	@Email
	String email,

	@NotBlank
	String password
) {
}
