package com.devnest.admin.dto.comment;

import com.devnest.course.entity.comment.CommentStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentResponse(
	UUID id,
	UUID lessonId,
	String lessonTitle,
	UUID courseId,
	String courseTitle,
	UUID studentId,
	String studentName,
	String content,
	Integer rating,
	CommentStatus status,
	boolean hidden,
	String moderationReason,
	OffsetDateTime removedAt,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
