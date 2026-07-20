package com.devnest.course.dto.student.metrics;

public record MetricsResponse(
	long totalCoursesEnrolled,
	long totalLessonsCompleted,
	double averageCourseProgress,
	long totalQuizzesCompleted,
	double averageQuizAccuracy,
	long totalCommentsMade
) {
}
