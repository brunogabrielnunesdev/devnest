package com.devnest.admin.service.comment;

import com.devnest.admin.dto.comment.CommentResponse;
import com.devnest.admin.dto.adminpage.AdminPageResponse;
import com.devnest.admin.service.acess.AccessService;
import com.devnest.common.exception.ConflictException;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.course.entity.comment.CommentStatus;
import com.devnest.course.entity.comment.Comment;
import com.devnest.course.repository.comment.CommentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminCommentService")
@RequiredArgsConstructor
public class CommentService {

	private final AccessService accessService;
	private final CommentRepository commentRepository;

	@Transactional(readOnly = true)
	public AdminPageResponse<CommentResponse> findAll(String query, int page, int size) {
		accessService.getAuthenticatedAdmin();

		var commentPage = commentRepository.findAdminComments(normalizeQuery(query), PageRequest.of(page, size));
		return new AdminPageResponse<>(
			commentPage.getContent().stream().map(this::toResponse).toList(),
			commentPage.getNumber(),
			commentPage.getSize(),
			commentPage.getTotalElements(),
			commentPage.getTotalPages()
		);
	}

	@Transactional(readOnly = true)
	public java.util.List<CommentResponse> findAllList(String query) {
		accessService.getAuthenticatedAdmin();
		String normalizedQuery = normalizeQuery(query);

		return commentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
			.stream()
			.filter(comment -> matchesQuery(comment, normalizedQuery))
			.map(this::toResponse)
			.toList();
	}

	@Transactional
	public CommentResponse hide(UUID commentId) {
		accessService.getAuthenticatedAdmin();
		Comment comment = findComment(commentId);
		ensureCommentCanBeToggled(comment);
		comment.setHidden(true);
		return toResponse(comment);
	}

	@Transactional
	public CommentResponse restore(UUID commentId) {
		accessService.getAuthenticatedAdmin();
		Comment comment = findComment(commentId);
		ensureCommentCanBeToggled(comment);
		comment.setHidden(false);
		return toResponse(comment);
	}

	private Comment findComment(UUID commentId) {
		return commentRepository.findById(commentId)
			.orElseThrow(() -> new ResourceNotFoundException("Comment not found."));
	}

	private void ensureCommentCanBeToggled(Comment comment) {
		if (comment.getStatus() != CommentStatus.VISIBLE) {
			throw new ConflictException("Only visible comments can be hidden or restored.");
		}
	}

	private String normalizeQuery(String query) {
		if (query == null || query.isBlank()) {
			return null;
		}
		return query.trim().toLowerCase();
	}

	private boolean matchesQuery(Comment comment, String query) {
		if (query == null) {
			return true;
		}

		String content = comment.getContent() != null ? comment.getContent().toLowerCase() : "";
		return content.contains(query);
	}

	private CommentResponse toResponse(Comment comment) {
		var lesson = comment.getLesson();
		var course = lesson.getModule().getCourse();
		return new CommentResponse(
			comment.getId(),
			lesson.getId(),
			lesson.getTitle(),
			course.getId(),
			course.getTitle(),
			comment.getStudent().getId(),
			comment.getStudent().getProfile().getDisplayName(),
			comment.getContent(),
			comment.getRating(),
			comment.getStatus(),
			comment.isHidden(),
			comment.getModerationReason(),
			comment.getRemovedAt(),
			comment.getCreatedAt(),
			comment.getUpdatedAt()
		);
	}
}
