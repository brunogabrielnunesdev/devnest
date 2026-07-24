package com.devnest.community.dto.post;

import com.devnest.community.entity.post.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record PostRequest(
		@NotBlank
		@Size(max = 160)
		String title,

		@NotBlank
		@Size(max = 20000)
		String content,

		@NotNull
		PostType type,

		UUID projectId,

		UUID courseId,

		@Size(max = 10)
		Set<UUID> tagIds
) {
}
