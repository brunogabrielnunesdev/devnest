package com.devnest.course.service.lesson;

import com.devnest.common.exception.ForbiddenException;
import com.devnest.course.dto.course.progress.CourseProgressSummaryResponse;
import com.devnest.course.dto.lesson.LessonProgressResponse;
import com.devnest.course.entity.course.CourseEnrollment;
import com.devnest.course.entity.lesson.LessonProgress;
import com.devnest.course.repository.lesson.LessonProgressRepository;
import com.devnest.course.repository.lesson.LessonRepository;
import com.devnest.course.repository.quiz.QuizAttemptRepository;
import com.devnest.course.repository.quiz.QuizRepository;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.devnest.course.service.course.CourseEnrollmentService;
import com.devnest.course.service.student.StudentLearningAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonProgressService {

	private final StudentLearningAccessService accessService;
	private final CourseEnrollmentService courseEnrollmentService;
	private final LessonProgressRepository lessonProgressRepository;
	private final LessonRepository lessonRepository;
	private final QuizRepository quizRepository;
	private final QuizAttemptRepository quizAttemptRepository;

	@Transactional
	public LessonProgressResponse completeLesson(UUID courseId, UUID lessonId) {
		var student = accessService.getAuthenticatedStudent();
		CourseEnrollment enrollment = courseEnrollmentService.getActiveOrCompletedEnrollment(courseId);
		var lesson = accessService.getPublishedCourseLesson(courseId, lessonId);
		ensureLessonCanBeCompleted(student.getId(), lesson.getId());

		LessonProgress progress = lessonProgressRepository.findByStudentIdAndLessonId(student.getId(), lesson.getId())
			.orElseGet(() -> {
				LessonProgress created = new LessonProgress();
				created.setStudent(student);
				created.setLesson(lesson);
				return created;
			});

		progress.setCompleted(true);
		if (progress.getCompletedAt() == null) {
			progress.setCompletedAt(OffsetDateTime.now());
		}

		LessonProgress saved = lessonProgressRepository.save(progress);

		long totalLessons = lessonRepository.countByModuleCourseId(courseId);
		long completedLessons = lessonProgressRepository.countByStudentIdAndLessonModuleCourseIdAndCompletedTrue(student.getId(), courseId);

		if (totalLessons > 0 && completedLessons == totalLessons) {
			courseEnrollmentService.completeEnrollmentIfNeeded(enrollment);
		}

		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public CourseProgressSummaryResponse getCourseProgress(UUID courseId) {
		var student = accessService.getAuthenticatedStudent();
		CourseEnrollment enrollment = courseEnrollmentService.getActiveOrCompletedEnrollment(courseId);
		long totalLessons = lessonRepository.countByModuleCourseId(courseId);
		long completedLessons = lessonProgressRepository.countByStudentIdAndLessonModuleCourseIdAndCompletedTrue(student.getId(), courseId);

		return new CourseProgressSummaryResponse(
			courseId,
			student.getId(),
			enrollment.getStatus(),
			totalLessons,
			completedLessons
		);
	}

	private void ensureLessonCanBeCompleted(UUID studentId, UUID lessonId) {
		quizRepository.findByLessonId(lessonId).ifPresent(quiz -> {
			if (!quizAttemptRepository.existsByQuizIdAndStudentIdAndPassedTrue(quiz.getId(), studentId)) {
				throw new ForbiddenException("Student must pass the lesson quiz before completing this lesson.");
			}
		});
	}

	private LessonProgressResponse toResponse(LessonProgress progress) {
		return new LessonProgressResponse(
			progress.getId(),
			progress.getLesson().getId(),
			progress.getStudent().getId(),
			progress.getCompleted(),
			progress.getCompletedAt(),
			progress.getCreatedAt(),
			progress.getUpdatedAt()
		);
	}
}

