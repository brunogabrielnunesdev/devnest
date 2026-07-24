package com.devnest.community.controller.reaction;

import com.devnest.community.dto.reaction.ReactionRequest;
import com.devnest.community.dto.reaction.ReactionResponse;
import com.devnest.community.dto.reaction.ReactionSummaryResponse;
import com.devnest.community.service.reaction.ReactionService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("communityReactionController")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/community")
public class ReactionController {

	private final ReactionService reactionService;

	@PutMapping("/posts/{postId}/reaction")
	public ResponseEntity<ReactionResponse> reactToPost(
			@PathVariable UUID postId,
			@Valid @RequestBody ReactionRequest request
	) {
		return ResponseEntity.ok(reactionService.reactToPost(postId, request));
	}

	@DeleteMapping("/posts/{postId}/reaction")
	public ResponseEntity<Void> removeFromPost(@PathVariable UUID postId) {
		reactionService.removeFromPost(postId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/posts/{postId}/reactions")
	public ResponseEntity<ReactionSummaryResponse> summarizePost(@PathVariable UUID postId) {
		return ResponseEntity.ok(reactionService.summarizePost(postId));
	}

	@PutMapping("/comments/{commentId}/reaction")
	public ResponseEntity<ReactionResponse> reactToComment(
			@PathVariable UUID commentId,
			@Valid @RequestBody ReactionRequest request
	) {
		return ResponseEntity.ok(reactionService.reactToComment(commentId, request));
	}

	@DeleteMapping("/comments/{commentId}/reaction")
	public ResponseEntity<Void> removeFromComment(@PathVariable UUID commentId) {
		reactionService.removeFromComment(commentId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/comments/{commentId}/reactions")
	public ResponseEntity<ReactionSummaryResponse> summarizeComment(@PathVariable UUID commentId) {
		return ResponseEntity.ok(reactionService.summarizeComment(commentId));
	}
}
