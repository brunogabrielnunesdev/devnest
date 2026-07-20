package com.devnest.project.dto.note;

import jakarta.validation.constraints.NotBlank;

public record NoteCreateRequest(
	@NotBlank
	String content
) {
}
