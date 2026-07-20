package com.devnest.course.service.comment;

import com.devnest.common.exception.ConflictException;
import com.devnest.common.exception.ForbiddenException;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.course.entity.lesson.Lesson;
import com.devnest.course.service.course.CourseAuthoringAccessService;
import com.devnest.course.service.course.CourseEnrollmentService;
import com.devnest.course.service.student.StudentLearningAccessService;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import com.devnest.auth.security.useridentity.CustomUserProvider;
import com.devnest.course.dto.comment.CommentResponse;
import com.devnest.course.entity.comment.CommentStatus;
import com.devnest.course.entity.comment.Comment;
import com.devnest.course.entity.lesson.LessonProgress;
import com.devnest.course.repository.comment.CommentRepository;
import com.devnest.course.repository.lesson.LessonProgressRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CustomUserProvider customUserProvider;
	private final StudentLearningAccessService studentLearningAccessService;
	private final CourseAuthoringAccessService courseAuthoringAccessService;
	private final CourseEnrollmentService courseEnrollmentService;
	private final LessonProgressRepository lessonProgressRepository;
	private final CommentRepository commentRepository;

	@Transactional
	public CommentResponse create(UUID courseId, UUID lessonId, String content, Integer rating) {
		String normalizedContent = content.trim();
		if (normalizedContent.isBlank()) {
			throw new ConflictException("Comment content must not be blank.");
		}

		User student = studentLearningAccessService.getAuthenticatedStudent();
		courseEnrollmentService.getActiveOrCompletedEnrollment(courseId);
		var lesson = studentLearningAccessService.getPublishedCourseLesson(courseId, lessonId);

		LessonProgress progress = lessonProgressRepository.findByStudentIdAndLessonId(student.getId(), lesson.getId())
			.orElseThrow(() -> new ForbiddenException("Student must complete the lesson before commenting."));

		if (!Boolean.TRUE.equals(progress.getCompleted())) {
			throw new ForbiddenException("Student must complete the lesson before commenting.");
		}

		Comment comment = new Comment();
		comment.setLesson(lesson);
		comment.setStudent(student);
		comment.setContent(normalizedContent);
		comment.setRating(rating);
		comment.setStatus(CommentStatus.VISIBLE);
		comment.setHidden(false);

		return toResponse(commentRepository.save(comment));
	}

	@Transactional(readOnly = true)
	public List<CommentResponse> findVisibleComments(UUID courseId, UUID lessonId) {
		User user = customUserProvider.getAuthenticatedUser();
		if (user.getRole() == UserRole.STUDENT) {
			courseEnrollmentService. getActiveOrCompletedEnrollment(courseId);
		}

		var lesson = studentLearningAccessService.getPublishedCourseLesson(courseId, lessonId);
		return commentRepository.findAllByLessonIdAndStatusAndHiddenFalseOrderByCreatedAtDesc(lesson.getId(), CommentStatus.VISIBLE)
			.stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional
	public CommentResponse moderateByTeacher(UUID courseId, UUID moduleId, UUID lessonId, UUID commentId, String reason) {
		String normalizedReason = reason.trim();
		if (normalizedReason.isBlank()) {
			throw new ConflictException("Moderation reason must not be blank.");
		}

		User teacher = customUserProvider.getAuthenticatedUser();
		if (teacher.getRole() != UserRole.TEACHER) {
			throw new ForbiddenException("Only teachers can moderate lesson comments.");
		}

		var lesson = getOwnedLessonForModeration(courseId, moduleId, lessonId);
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new ResourceNotFoundException("Comment not found."));

		if (!comment.getLesson().getId().equals(lesson.getId())) {
			throw new ResourceNotFoundException("Comment not found.");
		}
		ensureCommentIsModeratable(comment);

		comment.setStatus(CommentStatus.REMOVED_BY_TEACHER);
		comment.setModerationReason(normalizedReason);
		comment.setRemovedBy(teacher);
		comment.setRemovedAt(java.time.OffsetDateTime.now());
		return toResponse(comment);
	}

	@Transactional
	public CommentResponse moderateByAdmin(UUID commentId, String reason) {
		String normalizedReason = reason.trim();
		if (normalizedReason.isBlank()) {
			throw new ConflictException("Moderation reason must not be blank.");
		}

		User admin = customUserProvider.getAuthenticatedUser();
		if (admin.getRole() != UserRole.ADMIN) {
			throw new ForbiddenException("Only admins can moderate lesson comments.");
		}

		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new ResourceNotFoundException("Comment not found."));
		ensureCommentIsModeratable(comment);

		comment.setStatus(CommentStatus.REMOVED_BY_ADMIN);
		comment.setModerationReason(normalizedReason);
		comment.setRemovedBy(admin);
		comment.setRemovedAt(java.time.OffsetDateTime.now());
		return toResponse(comment);
	}

	private Lesson getOwnedLessonForModeration(UUID courseId, UUID moduleId, UUID lessonId) {
		return courseAuthoringAccessService.getOwnedLesson(courseId, moduleId, lessonId);
	}

	private void ensureCommentIsModeratable(Comment comment) {
		if (comment.getStatus() != CommentStatus.VISIBLE) {
			throw new ConflictException("Comment has already been moderated.");
		}
	}

	private CommentResponse toResponse(Comment comment) {
		return new CommentResponse(
			comment.getId(),
			comment.getLesson().getId(),
			comment.getStudent().getId(),
			comment.getStudent().getProfile().getDisplayName(),
			comment.getContent(),
			comment.getRating(),
			comment.getStatus(),
			comment.getModerationReason(),
			comment.getRemovedBy() != null ? comment.getRemovedBy().getId() : null,
			comment.getRemovedAt(),
			comment.getCreatedAt(),
			comment.getUpdatedAt()
		);
	}
}

