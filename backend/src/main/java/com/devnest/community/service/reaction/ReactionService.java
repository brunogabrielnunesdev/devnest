package com.devnest.community.service.reaction;

import com.devnest.community.dto.reaction.ReactionRequest;
import com.devnest.community.dto.reaction.ReactionResponse;
import com.devnest.community.dto.reaction.ReactionSummaryResponse;
import com.devnest.community.entity.comment.Comment;
import com.devnest.community.entity.post.ContentStatus;
import com.devnest.community.entity.post.Post;
import com.devnest.community.entity.reaction.Reaction;
import com.devnest.community.entity.reaction.ReactionType;
import com.devnest.community.exception.comment.CommentNotFoundException;
import com.devnest.community.exception.post.PostNotFoundException;
import com.devnest.community.exception.reaction.ReactionConflictException;
import com.devnest.community.mapper.reaction.ReactionMapper;
import com.devnest.community.repository.comment.CommentRepository;
import com.devnest.community.repository.post.PostRepository;
import com.devnest.community.repository.reaction.ReactionCount;
import com.devnest.community.repository.reaction.ReactionRepository;
import com.devnest.community.service.access.AccessService;
import com.devnest.community.service.ratelimit.CommunityRateLimitService;
import com.devnest.community.service.concurrency.CommunityActorLockService;
import com.devnest.identity.entity.User;
import com.devnest.community.service.userrelation.UserRelationAccessService;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Service("communityReactionService")
@RequiredArgsConstructor
public class ReactionService {

	private final AccessService accessService;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final ReactionRepository reactionRepository;
	private final ReactionMapper reactionMapper;
	private final UserRelationAccessService userRelationAccessService;
	private final CommunityRateLimitService rateLimitService;
	private final CommunityActorLockService actorLockService;

	@Transactional
	public ReactionResponse reactToPost(UUID postId, ReactionRequest request) {
		User user = accessService.getAuthenticatedUser();
		Post post = findActivePost(postId);
		userRelationAccessService.validateInteraction(user.getId(), post.getAuthor().getId());
		actorLockService.lock(user.getId());
		Reaction reaction = reactionRepository.findByUserIdAndPostId(user.getId(), postId)
				.orElse(null);
		if (reaction != null && reaction.getType() == request.type()) {
			return reactionMapper.toResponse(reaction);
		}
		rateLimitService.validateReactionChange(user.getId());
		reaction = reaction == null
				? Reaction.forPost(user, post, request.type())
				: changeType(reaction, request.type());
		return reactionMapper.toResponse(save(reaction));
	}

	@Transactional
	public ReactionResponse reactToComment(UUID commentId, ReactionRequest request) {
		User user = accessService.getAuthenticatedUser();
		Comment comment = findActiveComment(commentId);
		userRelationAccessService.validateInteraction(user.getId(), comment.getAuthor().getId());
		actorLockService.lock(user.getId());
		Reaction reaction = reactionRepository.findByUserIdAndCommentId(user.getId(), commentId)
				.orElse(null);
		if (reaction != null && reaction.getType() == request.type()) {
			return reactionMapper.toResponse(reaction);
		}
		rateLimitService.validateReactionChange(user.getId());
		reaction = reaction == null
				? Reaction.forComment(user, comment, request.type())
				: changeType(reaction, request.type());
		return reactionMapper.toResponse(save(reaction));
	}

	@Transactional
	public void removeFromPost(UUID postId) {
		User user = accessService.getAuthenticatedUser();
		findActivePost(postId);
		reactionRepository.findByUserIdAndPostId(user.getId(), postId)
				.ifPresent(reactionRepository::delete);
	}

	@Transactional
	public void removeFromComment(UUID commentId) {
		User user = accessService.getAuthenticatedUser();
		findActiveComment(commentId);
		reactionRepository.findByUserIdAndCommentId(user.getId(), commentId)
				.ifPresent(reactionRepository::delete);
	}

	@Transactional(readOnly = true)
	public ReactionSummaryResponse summarizePost(UUID postId) {
		User user = accessService.getAuthenticatedUser();
		findActivePost(postId);
		ReactionType current = reactionRepository.findByUserIdAndPostId(user.getId(), postId)
				.map(Reaction::getType)
				.orElse(null);
		return summarize(reactionRepository.countByPostGroupedByType(postId), current);
	}

	@Transactional(readOnly = true)
	public ReactionSummaryResponse summarizeComment(UUID commentId) {
		User user = accessService.getAuthenticatedUser();
		findActiveComment(commentId);
		ReactionType current = reactionRepository.findByUserIdAndCommentId(user.getId(), commentId)
				.map(Reaction::getType)
				.orElse(null);
		return summarize(reactionRepository.countByCommentGroupedByType(commentId), current);
	}

	private Post findActivePost(UUID postId) {
		return postRepository.findByIdAndStatus(postId, ContentStatus.ACTIVE)
				.orElseThrow(PostNotFoundException::new);
	}

	private Comment findActiveComment(UUID commentId) {
		return commentRepository.findByIdAndStatus(commentId, ContentStatus.ACTIVE)
				.orElseThrow(CommentNotFoundException::new);
	}

	private Reaction changeType(Reaction reaction, ReactionType type) {
		reaction.changeType(type);
		return reaction;
	}

	private Reaction save(Reaction reaction) {
		try {
			return reactionRepository.saveAndFlush(reaction);
		} catch (DataIntegrityViolationException exception) {
			throw new ReactionConflictException();
		}
	}

	private ReactionSummaryResponse summarize(List<ReactionCount> groupedCounts, ReactionType current) {
		Map<ReactionType, Long> counts = new EnumMap<>(ReactionType.class);
		for (ReactionType type : ReactionType.values()) {
			counts.put(type, 0L);
		}
		groupedCounts.forEach(count -> counts.put(count.getType(), count.getTotal()));
		long total = counts.values().stream().mapToLong(Long::longValue).sum();
		return new ReactionSummaryResponse(counts, total, current);
	}
}
