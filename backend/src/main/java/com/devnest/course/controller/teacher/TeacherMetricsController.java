package com.devnest.course.controller.teacher;

import com.devnest.course.dto.teacher.TeacherMetricsResponse;
import com.devnest.course.service.teacher.TeacherMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
@RequestMapping("/teacher/metrics")
public class TeacherMetricsController {

	private final TeacherMetricsService teacherMetricsService;

	@GetMapping
	public ResponseEntity<TeacherMetricsResponse> getMetrics() {
		return ResponseEntity.ok(teacherMetricsService.getMetrics());
	}
}
