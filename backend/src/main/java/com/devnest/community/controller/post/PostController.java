package com.devnest.community.controller.post;

import com.devnest.community.dto.post.PostRequest;
import com.devnest.community.dto.post.PostResponse;
import com.devnest.community.dto.post.PostUpdateRequest;
import com.devnest.community.service.post.PostService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/community")
public class PostController {

	private final PostService postService;

	@GetMapping("/posts")
	public ResponseEntity<Page<PostResponse>> findFeed(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(postService.findFeed(pageable));
	}

	@GetMapping("/posts/{postId}")
	public ResponseEntity<PostResponse> findById(@PathVariable UUID postId) {
		return ResponseEntity.ok(postService.findById(postId));
	}

	@PostMapping("/forums/{forumId}/posts")
	public ResponseEntity<PostResponse> create(
			@PathVariable UUID forumId,
			@Valid @RequestBody PostRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(forumId, request));
	}

	@PatchMapping("/posts/{postId}")
	public ResponseEntity<PostResponse> update(
			@PathVariable UUID postId,
			@Valid @RequestBody PostUpdateRequest request
	) {
		return ResponseEntity.ok(postService.update(postId, request));
	}

	@DeleteMapping("/posts/{postId}")
	public ResponseEntity<Void> remove(@PathVariable UUID postId) {
		postService.remove(postId, "Removed by author.");
		return ResponseEntity.noContent().build();
	}
}
