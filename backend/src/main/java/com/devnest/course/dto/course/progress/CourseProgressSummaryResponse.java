package com.devnest.course.dto.course.progress;

import com.devnest.course.entity.course.EnrollmentStatus;
import java.util.UUID;

public record CourseProgressSummaryResponse(
	UUID courseId,
	UUID studentId,
	EnrollmentStatus enrollmentStatus,
	long totalLessons,
	long completedLessons
) {
}

