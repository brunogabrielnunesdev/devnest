package com.devnest.course.dto.comment;

import com.devnest.course.entity.comment.CommentStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentResponse(
	UUID id,
	UUID lessonId,
	UUID studentId,
	String studentDisplayName,
	String content,
	Integer rating,
	CommentStatus status,
	String moderationReason,
	UUID removedBy,
	OffsetDateTime removedAt,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}

