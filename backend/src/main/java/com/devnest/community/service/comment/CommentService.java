package com.devnest.community.service.comment;

import com.devnest.community.dto.comment.CommentRequest;
import com.devnest.community.dto.comment.CommentResponse;
import com.devnest.community.entity.comment.Comment;
import com.devnest.community.entity.post.ContentStatus;
import com.devnest.community.entity.post.Post;
import com.devnest.community.exception.comment.CommentUnavailableException;
import com.devnest.community.exception.post.PostNotFoundException;
import com.devnest.community.mapper.comment.CommentMapper;
import com.devnest.community.repository.comment.CommentRepository;
import com.devnest.community.repository.post.PostRepository;
import com.devnest.community.service.access.AccessService;
import com.devnest.community.service.content.ContentFilter;
import com.devnest.community.service.content.ContentFilterResult;
import com.devnest.community.service.ratelimit.CommunityRateLimitService;
import com.devnest.community.service.userrelation.UserRelationAccessService;
import com.devnest.identity.entity.User;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("communityCommentService")
@RequiredArgsConstructor
public class CommentService {

	private final AccessService accessService;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final CommentMapper commentMapper;
	private final ContentFilter contentFilter;
	private final Clock communityClock;
	private final UserRelationAccessService userRelationAccessService;
	private final CommunityRateLimitService rateLimitService;

	@Transactional
	public CommentResponse create(UUID postId, CommentRequest request) {
		Post post = findInteractivePost(postId);
		User author = accessService.getAuthenticatedUser();
		userRelationAccessService.validateInteraction(author.getId(), post.getAuthor().getId());
		rateLimitService.validateCommentCreation(author.getId());
		Comment comment = Comment.create(post, author, request.content());
		applyContentFilter(comment, request.content());
		return commentMapper.toResponse(commentRepository.save(comment));
	}

	@Transactional(readOnly = true)
	public Page<CommentResponse> findByPost(UUID postId, Pageable pageable) {
		if (!postRepository.existsByIdAndStatus(postId, ContentStatus.ACTIVE)) {
			throw new PostNotFoundException();
		}
		return commentRepository.findAllByPostIdAndStatus(postId, ContentStatus.ACTIVE, pageable)
				.map(commentMapper::toResponse);
	}

	@Transactional
	public CommentResponse update(UUID commentId, CommentRequest request) {
		Comment comment = accessService.getCommentForManagement(commentId);
		validateManageable(comment);
		findInteractivePost(comment.getPost().getId());
		comment.update(request.content());
		applyContentFilter(comment, request.content());
		return commentMapper.toResponse(comment);
	}

	@Transactional
	public void remove(UUID commentId, String reason) {
		Comment comment = accessService.getCommentForManagement(commentId);
		validateManageable(comment);
		comment.remove(accessService.getAuthenticatedUser(), reason, OffsetDateTime.now(communityClock));
	}

	private Post findInteractivePost(UUID postId) {
		Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
		if (post.getStatus() != ContentStatus.ACTIVE || post.isCommentsLocked()) {
			throw new CommentUnavailableException("This post does not accept comments.");
		}
		return post;
	}

	private void validateManageable(Comment comment) {
		if (comment.getStatus() != ContentStatus.ACTIVE
				&& comment.getStatus() != ContentStatus.HELD_FOR_REVIEW) {
			throw new CommentUnavailableException("This comment can no longer be changed.");
		}
	}

	private void applyContentFilter(Comment comment, String content) {
		ContentFilterResult result = contentFilter.evaluate(content);
		comment.applyContentFilter(result.requiresReview(), result.ruleVersion(), result.matchedTerms());
	}
}
