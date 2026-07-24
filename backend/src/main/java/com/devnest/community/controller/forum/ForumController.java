package com.devnest.community.controller.forum;

import com.devnest.community.dto.forum.ForumResponse;
import com.devnest.community.dto.post.PostResponse;
import com.devnest.community.service.forum.ForumService;
import com.devnest.community.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/community/forums")
public class ForumController {

	private final ForumService forumService;
	private final PostService postService;

	@GetMapping
	public ResponseEntity<Page<ForumResponse>> findActive(
			@PageableDefault(size = 20, sort = "name") Pageable pageable
	) {
		return ResponseEntity.ok(forumService.findActive(pageable));
	}

	@GetMapping("/{slug}")
	public ResponseEntity<ForumResponse> findBySlug(@PathVariable String slug) {
		return ResponseEntity.ok(forumService.findActiveBySlug(slug));
	}

	@GetMapping("/{slug}/posts")
	public ResponseEntity<Page<PostResponse>> findPosts(
			@PathVariable String slug,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		ForumResponse forum = forumService.findActiveBySlug(slug);
		return ResponseEntity.ok(postService.findForumFeed(forum.id(), pageable));
	}
}
