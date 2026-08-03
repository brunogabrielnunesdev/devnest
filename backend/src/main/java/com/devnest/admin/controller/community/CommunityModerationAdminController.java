package com.devnest.admin.controller.community;

import com.devnest.community.dto.moderation.ModerationActionRequest;
import com.devnest.community.dto.moderation.ModerationActionResponse;
import com.devnest.community.dto.moderation.ModerationCaseResponse;
import com.devnest.community.entity.moderation.ModerationCaseStatus;
import com.devnest.community.service.moderation.ModerationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/community/moderation/cases")
public class CommunityModerationAdminController {

	private final ModerationService moderationService;

	@GetMapping
	public ResponseEntity<Page<ModerationCaseResponse>> findCases(
			@RequestParam(required = false) ModerationCaseStatus status,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
	) {
		return ResponseEntity.ok(moderationService.findCases(status, pageable));
	}

	@GetMapping("/{caseId}/actions")
	public ResponseEntity<List<ModerationActionResponse>> findActions(@PathVariable UUID caseId) {
		return ResponseEntity.ok(moderationService.findActions(caseId));
	}

	@PostMapping("/{caseId}/actions")
	public ResponseEntity<ModerationActionResponse> perform(
			@PathVariable UUID caseId,
			@Valid @RequestBody ModerationActionRequest request
	) {
		return ResponseEntity.ok(moderationService.perform(caseId, request));
	}
}
