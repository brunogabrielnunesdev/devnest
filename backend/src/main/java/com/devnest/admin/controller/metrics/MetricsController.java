package com.devnest.admin.controller.metrics;

import com.devnest.admin.dto.metrics.MetricsResponse;
import com.devnest.admin.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminMetricsController")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/metrics")
public class MetricsController {

	private final MetricsService metricsService;

	@GetMapping
	public ResponseEntity<MetricsResponse> getMetrics() {
		return ResponseEntity.ok(metricsService.getMetrics());
	}
}
