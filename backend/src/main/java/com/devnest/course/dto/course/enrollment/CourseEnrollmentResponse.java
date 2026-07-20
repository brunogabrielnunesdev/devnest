package com.devnest.course.dto.course.enrollment;

import com.devnest.course.entity.course.EnrollmentStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CourseEnrollmentResponse(
	UUID id,
	UUID courseId,
	UUID studentId,
	EnrollmentStatus status,
	OffsetDateTime enrolledAt,
	OffsetDateTime completedAt,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt
) {
}

