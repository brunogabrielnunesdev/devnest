package com.devnest.auth.dto.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
	@NotBlank(message = "Email nao pode estar vazio")
	@Email
	@Size(max = 320)
	String email,

	@NotBlank(message = "")
	@Size(min = 8, max = 72)
	String password,

	@NotBlank(message = "")
	@Size(max = 80)
	String displayName
) {
}
