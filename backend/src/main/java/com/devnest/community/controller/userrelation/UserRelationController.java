package com.devnest.community.controller.userrelation;

import com.devnest.community.dto.userrelation.UserRelationResponse;
import com.devnest.community.service.userrelation.UserRelationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/community/users")
public class UserRelationController {

	private final UserRelationService userRelationService;

	@PutMapping("/{userId}/block")
	public ResponseEntity<UserRelationResponse> block(@PathVariable UUID userId) {
		return ResponseEntity.ok(userRelationService.block(userId));
	}

	@DeleteMapping("/{userId}/block")
	public ResponseEntity<Void> unblock(@PathVariable UUID userId) {
		userRelationService.unblock(userId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/blocked")
	public ResponseEntity<Page<UserRelationResponse>> findBlocked(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(userRelationService.findBlocked(pageable));
	}

	@PutMapping("/{userId}/mute")
	public ResponseEntity<UserRelationResponse> mute(@PathVariable UUID userId) {
		return ResponseEntity.ok(userRelationService.mute(userId));
	}

	@DeleteMapping("/{userId}/mute")
	public ResponseEntity<Void> unmute(@PathVariable UUID userId) {
		userRelationService.unmute(userId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/muted")
	public ResponseEntity<Page<UserRelationResponse>> findMuted(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(userRelationService.findMuted(pageable));
	}
}
