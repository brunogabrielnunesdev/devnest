package com.devnest.community.dto.comment;

import com.devnest.community.entity.post.ContentStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentResponse(
		UUID id,
		UUID postId,
		UUID authorId,
		String content,
		ContentStatus status,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
