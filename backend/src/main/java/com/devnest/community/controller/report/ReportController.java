package com.devnest.community.controller.report;

import com.devnest.community.dto.report.ReportRequest;
import com.devnest.community.dto.report.ReportResponse;
import com.devnest.community.service.report.ReportService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/community")
public class ReportController {

	private final ReportService reportService;

	@PostMapping("/posts/{postId}/reports")
	public ResponseEntity<ReportResponse> reportPost(
			@PathVariable UUID postId,
			@Valid @RequestBody ReportRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(reportService.reportPost(postId, request));
	}

	@PostMapping("/comments/{commentId}/reports")
	public ResponseEntity<ReportResponse> reportComment(
			@PathVariable UUID commentId,
			@Valid @RequestBody ReportRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(reportService.reportComment(commentId, request));
	}
}
