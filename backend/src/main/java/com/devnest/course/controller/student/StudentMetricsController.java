package com.devnest.course.controller.student;

import com.devnest.course.dto.student.metrics.MetricsResponse;
import com.devnest.course.service.student.metrics.StudentMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@RequestMapping("/student/metrics")
public class StudentMetricsController {

	private final StudentMetricsService studentMetricsService;

	@GetMapping
	public ResponseEntity<MetricsResponse> getMetrics() {
		return ResponseEntity.ok(studentMetricsService.getMetrics());
	}
}
