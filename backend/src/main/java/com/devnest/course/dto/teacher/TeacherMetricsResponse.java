package com.devnest.course.dto.teacher;

public record TeacherMetricsResponse(
	long totalCoursesCreated,
	long totalModules,
	long totalLessons,
	long totalStudentsEnrolled,
	double averageCourseRating,
	long totalCommentsReceived,
	long totalQuizzesCreated
) {
}
