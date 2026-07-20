package com.devnest.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
	@NotBlank
	@Size(max = 80)
	String displayName,

	@Size(max = 120)
	String fullName,

	@Size(max = 2000)
	String bio,

	@Size(max = 255)
	String avatarUrl,

	@Size(max = 255)
	String githubUrl,

	@Size(max = 255)
	String linkedinUrl,

	@Size(max = 255)
	String portfolioUrl,

	@Size(max = 120)
	String location
) {
}
