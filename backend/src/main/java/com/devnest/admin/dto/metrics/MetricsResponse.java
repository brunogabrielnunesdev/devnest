package com.devnest.admin.dto.metrics;

public record MetricsResponse(
	long totalUsers,
	long totalCourses,
	long totalComments
) {
}
