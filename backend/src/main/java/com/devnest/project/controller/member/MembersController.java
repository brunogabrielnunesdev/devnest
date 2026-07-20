package com.devnest.project.controller.member;

import com.devnest.project.dto.members.MemberCreateRequest;
import com.devnest.project.dto.members.MemberResponse;
import com.devnest.project.dto.members.MemberUpdateRequest;
import com.devnest.project.service.member.MemberService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/projects/{projectId}/members")
public class MembersController {

	private final MemberService memberService;

	@PostMapping
	public ResponseEntity<MemberResponse> create(
		@PathVariable UUID projectId,
		@Valid @RequestBody MemberCreateRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(projectId, request));
	}

	@GetMapping
	public ResponseEntity<List<MemberResponse>> findAll(@PathVariable UUID projectId) {
		return ResponseEntity.ok(memberService.findAll(projectId));
	}

	@PatchMapping("/{memberId}")
	public ResponseEntity<MemberResponse> update(
		@PathVariable UUID projectId,
		@PathVariable UUID memberId,
		@Valid @RequestBody MemberUpdateRequest request
	) {
		return ResponseEntity.ok(memberService.update(projectId, memberId, request));
	}

	@DeleteMapping("/{memberId}")
	public ResponseEntity<Void> delete(@PathVariable UUID projectId, @PathVariable UUID memberId) {
		memberService.delete(projectId, memberId);
		return ResponseEntity.noContent().build();
	}
}
