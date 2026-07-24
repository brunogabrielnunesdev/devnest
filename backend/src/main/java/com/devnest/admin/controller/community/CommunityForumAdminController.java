package com.devnest.admin.controller.community;

import com.devnest.community.dto.forum.ForumRequest;
import com.devnest.community.dto.forum.ForumResponse;
import com.devnest.community.service.forum.ForumService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/community/forums")
public class CommunityForumAdminController {

	private final ForumService forumService;

	@PostMapping
	public ResponseEntity<ForumResponse> create(
			@Valid @RequestBody ForumRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(forumService.create(request));
	}

	@PatchMapping("/{forumId}")
	public ResponseEntity<ForumResponse> update(
			@PathVariable UUID forumId,
			@Valid @RequestBody ForumRequest request
	) {
		return ResponseEntity.ok(forumService.update(forumId, request));
	}

	@PatchMapping("/{forumId}/archive")
	public ResponseEntity<ForumResponse> archive(@PathVariable UUID forumId) {
		return ResponseEntity.ok(forumService.archive(forumId));
	}

	@PatchMapping("/{forumId}/restore")
	public ResponseEntity<ForumResponse> restore(@PathVariable UUID forumId) {
		return ResponseEntity.ok(forumService.restore(forumId));
	}
}
