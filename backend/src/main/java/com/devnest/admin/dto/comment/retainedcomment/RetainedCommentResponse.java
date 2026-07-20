package com.devnest.admin.dto.comment.retainedcomment;

import com.devnest.course.entity.comment.CommentStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RetainedCommentResponse(
	UUID id,
	String content,
	Integer rating,
	CommentStatus status,
	String moderationReason,
	UUID studentId,
	String studentDisplayName,
	UUID courseId,
	String courseTitle,
	UUID lessonId,
	String lessonTitle,
	OffsetDateTime removedAt,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}
