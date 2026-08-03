package com.devnest.community.service.moderation;

import com.devnest.community.dto.moderation.ModerationActionRequest;
import com.devnest.community.dto.moderation.ModerationActionResponse;
import com.devnest.community.dto.moderation.ModerationCaseResponse;
import com.devnest.community.entity.comment.Comment;
import com.devnest.community.entity.moderation.ModerationAction;
import com.devnest.community.entity.moderation.ModerationActionType;
import com.devnest.community.entity.moderation.ModerationCase;
import com.devnest.community.entity.moderation.ModerationCaseStatus;
import com.devnest.community.entity.post.ContentStatus;
import com.devnest.community.entity.post.Post;
import com.devnest.community.entity.report.Report;
import com.devnest.community.exception.moderation.ModerationCaseNotFoundException;
import com.devnest.community.exception.moderation.ModerationConflictException;
import com.devnest.community.repository.comment.CommentRepository;
import com.devnest.community.repository.moderation.ModerationActionRepository;
import com.devnest.community.repository.moderation.ModerationCaseRepository;
import com.devnest.community.repository.post.PostRepository;
import com.devnest.community.service.access.AccessService;
import com.devnest.identity.entity.User;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModerationService {

	private final AccessService accessService;
	private final ModerationCaseRepository caseRepository;
	private final ModerationActionRepository actionRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final Clock communityClock;

	public ModerationCase openCase(Report report, User moderator, OffsetDateTime openedAt) {
		return caseRepository.save(ModerationCase.open(report, moderator, openedAt));
	}

	@Transactional(readOnly = true)
	public Page<ModerationCaseResponse> findCases(ModerationCaseStatus status, Pageable pageable) {
		accessService.getAuthenticatedModerator();
		Page<ModerationCase> cases = status == null
				? caseRepository.findAll(pageable)
				: caseRepository.findAllByStatus(status, pageable);
		return cases.map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public List<ModerationActionResponse> findActions(UUID caseId) {
		accessService.getAuthenticatedModerator();
		if (!caseRepository.existsById(caseId)) {
			throw new ModerationCaseNotFoundException();
		}
		return actionRepository.findAllByModerationCaseIdOrderByPerformedAtAscIdAsc(caseId)
				.stream().map(this::toResponse).toList();
	}

	@Transactional
	public ModerationActionResponse perform(UUID caseId, ModerationActionRequest request) {
		User moderator = accessService.getAuthenticatedModerator();
		ModerationCase moderationCase = caseRepository.findByIdForUpdate(caseId)
				.orElseThrow(ModerationCaseNotFoundException::new);
		if (moderationCase.getStatus() != ModerationCaseStatus.OPEN) {
			throw new ModerationConflictException("Only open moderation cases accept actions.");
		}

		OffsetDateTime now = OffsetDateTime.now(communityClock);
		String previousState = stateOf(moderationCase);
		apply(moderationCase, request.action(), moderator, request.reason(), now);
		String newState = stateOf(moderationCase);
		ModerationAction action = ModerationAction.create(
				moderationCase,
				request.action(),
				moderator,
				request.reason(),
				previousState,
				newState,
				now
		);
		return toResponse(actionRepository.save(action));
	}

	private void apply(
			ModerationCase moderationCase,
			ModerationActionType action,
			User moderator,
			String reason,
			OffsetDateTime now
	) {
		if (action == ModerationActionType.RESOLVE_CASE) {
			moderationCase.resolve(moderator, now);
			return;
		}
		if (moderationCase.getPost() != null) {
			Post post = postRepository.findByIdForUpdate(moderationCase.getPost().getId())
					.orElseThrow(() -> new ModerationConflictException("Moderated post no longer exists."));
			applyToPost(post, action, moderator, reason, now);
			return;
		}
		Comment comment = commentRepository.findByIdForUpdate(moderationCase.getComment().getId())
				.orElseThrow(() -> new ModerationConflictException("Moderated comment no longer exists."));
		applyToComment(comment, action, moderator, reason, now);
	}

	private void applyToPost(Post post, ModerationActionType action, User moderator, String reason, OffsetDateTime now) {
		switch (action) {
			case HIDE -> {
				require(post.getStatus() == ContentStatus.ACTIVE, "Only active posts can be hidden.");
				post.hide();
			}
			case RESTORE -> {
				require(post.getStatus() == ContentStatus.HIDDEN || post.getStatus() == ContentStatus.REMOVED,
						"Only hidden or removed posts can be restored.");
				post.activate();
			}
			case REMOVE -> {
				require(post.getStatus() != ContentStatus.REMOVED, "Post is already removed.");
				post.remove(moderator, reason, now);
			}
			case LOCK_COMMENTS -> {
				require(post.getStatus() != ContentStatus.REMOVED && !post.isCommentsLocked(),
						"Post comments cannot be locked in the current state.");
				post.lockComments();
			}
			case UNLOCK_COMMENTS -> {
				require(post.getStatus() == ContentStatus.ACTIVE && post.isCommentsLocked(),
						"Post comments cannot be unlocked in the current state.");
				post.unlockComments();
			}
			case RESOLVE_CASE -> throw new IllegalStateException("Case resolution is handled separately.");
		}
	}

	private void applyToComment(Comment comment, ModerationActionType action, User moderator, String reason, OffsetDateTime now) {
		switch (action) {
			case HIDE -> {
				require(comment.getStatus() == ContentStatus.ACTIVE, "Only active comments can be hidden.");
				comment.hide();
			}
			case RESTORE -> {
				require(comment.getStatus() == ContentStatus.HIDDEN || comment.getStatus() == ContentStatus.REMOVED,
						"Only hidden or removed comments can be restored.");
				comment.activate();
			}
			case REMOVE -> {
				require(comment.getStatus() != ContentStatus.REMOVED, "Comment is already removed.");
				comment.remove(moderator, reason, now);
			}
			case LOCK_COMMENTS, UNLOCK_COMMENTS ->
					throw new ModerationConflictException("Comment targets do not support comment locking.");
			case RESOLVE_CASE -> throw new IllegalStateException("Case resolution is handled separately.");
		}
	}

	private void require(boolean condition, String message) {
		if (!condition) {
			throw new ModerationConflictException(message);
		}
	}

	private String stateOf(ModerationCase moderationCase) {
		if (moderationCase.getPost() != null) {
			Post post = moderationCase.getPost();
			return "case=" + moderationCase.getStatus() + ";content=" + post.getStatus()
					+ ";commentsLocked=" + post.isCommentsLocked();
		}
		return "case=" + moderationCase.getStatus() + ";content=" + moderationCase.getComment().getStatus();
	}

	private ModerationCaseResponse toResponse(ModerationCase moderationCase) {
		return new ModerationCaseResponse(
				moderationCase.getId(), moderationCase.getReport().getId(),
				moderationCase.getPost() == null ? null : moderationCase.getPost().getId(),
				moderationCase.getComment() == null ? null : moderationCase.getComment().getId(),
				moderationCase.getStatus(), moderationCase.getOpenedBy().getId(), moderationCase.getOpenedAt(),
				moderationCase.getResolvedBy() == null ? null : moderationCase.getResolvedBy().getId(),
				moderationCase.getResolvedAt()
		);
	}

	private ModerationActionResponse toResponse(ModerationAction action) {
		return new ModerationActionResponse(
				action.getId(), action.getModerationCase().getId(), action.getActionType(),
				action.getModerator().getId(), action.getReason(), action.getPreviousState(),
				action.getNewState(), action.getPerformedAt()
		);
	}
}
