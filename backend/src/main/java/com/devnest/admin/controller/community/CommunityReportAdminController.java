package com.devnest.admin.controller.community;

import com.devnest.community.dto.report.ReportResponse;
import com.devnest.community.dto.report.ReportReviewRequest;
import com.devnest.community.entity.report.ReportStatus;
import com.devnest.community.service.report.ReportService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/community/reports")
public class CommunityReportAdminController {

	private final ReportService reportService;

	@GetMapping
	public ResponseEntity<Page<ReportResponse>> findQueue(
			@RequestParam(required = false) ReportStatus status,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
			Pageable pageable
	) {
		return ResponseEntity.ok(reportService.findQueue(status, pageable));
	}

	@PatchMapping("/{reportId}")
	public ResponseEntity<ReportResponse> review(
			@PathVariable UUID reportId,
			@Valid @RequestBody ReportReviewRequest request
	) {
		return ResponseEntity.ok(reportService.review(reportId, request));
	}
}
