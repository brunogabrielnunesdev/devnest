package com.devnest.admin.service.lessoncomment;

import com.devnest.auth.security.useridentity.CustomUserProvider;
import com.devnest.common.exception.ConflictException;
import com.devnest.common.exception.ForbiddenException;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.course.entity.comment.CommentStatus;
import com.devnest.course.entity.comment.Comment;
import com.devnest.course.dto.comment.CommentResponse;
import com.devnest.course.repository.comment.CommentRepository;
import com.devnest.course.service.comment.CommentService;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import com.devnest.admin.dto.comment.retainedcomment.RetainedCommentResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminLessonCommentService")
@RequiredArgsConstructor
public class LessonCommentService {

	private static final List<CommentStatus> RETAINED_STATUSES = List.of(
		CommentStatus.HIDDEN_BY_FILTER,
		CommentStatus.REMOVED_BY_TEACHER
	);

	private final CustomUserProvider customUserProvider;
	private final CommentRepository commentRepository;
	private final CommentService commentService;

	@Transactional(readOnly = true)
	public List<RetainedCommentResponse> findRetainedComments() {
		ensureAdmin();
		return commentRepository.findAllByStatusInOrderByCreatedAtDesc(RETAINED_STATUSES)
			.stream()
			.map(this::toAdminResponse)
			.toList();
	}

	@Transactional
	public CommentResponse moderate(UUID commentId, String reason) {
		ensureAdmin();
		return commentService.moderateByAdmin(commentId, reason);
	}

	@Transactional
	public void deleteRetainedComment(UUID commentId) {
		ensureAdmin();
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new ResourceNotFoundException("Comment not found."));

		if (!RETAINED_STATUSES.contains(comment.getStatus())) {
			throw new ConflictException("Only retained comments can be deleted from the admin queue.");
		}

		commentRepository.delete(comment);
	}

	private void ensureAdmin() {
		User user = customUserProvider.getAuthenticatedUser();
		if (user.getRole() != UserRole.ADMIN) {
			throw new ForbiddenException("Only admins can manage retained lesson comments.");
		}
	}

	private RetainedCommentResponse toAdminResponse(Comment comment) {
		var lesson = comment.getLesson();
		var course = lesson.getModule().getCourse();
		return new RetainedCommentResponse(
			comment.getId(),
			comment.getContent(),
			comment.getRating(),
			comment.getStatus(),
			comment.getModerationReason(),
			comment.getStudent().getId(),
			comment.getStudent().getProfile().getDisplayName(),
			course.getId(),
			course.getTitle(),
			lesson.getId(),
			lesson.getTitle(),
			comment.getRemovedAt(),
			comment.getCreatedAt(),
			comment.getUpdatedAt()
		);
	}
}
