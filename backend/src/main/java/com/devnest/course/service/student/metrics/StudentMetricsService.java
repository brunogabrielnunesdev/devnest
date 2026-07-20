package com.devnest.course.service.student.metrics;

import com.devnest.course.dto.student.metrics.MetricsResponse;
import com.devnest.course.entity.course.EnrollmentStatus;
import com.devnest.course.repository.course.CourseEnrollmentRepository;
import com.devnest.course.repository.comment.CommentRepository;
import com.devnest.course.repository.lesson.LessonProgressRepository;
import com.devnest.course.repository.lesson.LessonRepository;
import com.devnest.course.repository.quiz.QuizAttemptRepository;
import java.util.List;

import com.devnest.course.service.student.StudentLearningAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentMetricsService {

	private static final List<EnrollmentStatus> ACTIVE_ENROLLMENTS = List.of(
		EnrollmentStatus.ACTIVE,
		EnrollmentStatus.COMPLETED
	);

	private final StudentLearningAccessService studentLearningAccessService;
	private final CourseEnrollmentRepository courseEnrollmentRepository;
	private final LessonRepository lessonRepository;
	private final LessonProgressRepository lessonProgressRepository;
	private final QuizAttemptRepository quizAttemptRepository;
	private final CommentRepository commentRepository;

	@Transactional(readOnly = true)
	public MetricsResponse getMetrics() {
		var student = studentLearningAccessService.getAuthenticatedStudent();
		var enrollments = courseEnrollmentRepository.findAllByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
			.filter(enrollment -> ACTIVE_ENROLLMENTS.contains(enrollment.getStatus()))
			.toList();

		double averageCourseProgress = 0.0;
		if (!enrollments.isEmpty()) {
			double progressSum = 0.0;
			for (var enrollment : enrollments) {
				long totalLessons = lessonRepository.countByModuleCourseId(enrollment.getCourse().getId());
				if (totalLessons == 0) {
					continue;
				}

				long completedLessons = lessonProgressRepository.countByStudentIdAndLessonModuleCourseIdAndCompletedTrue(
					student.getId(),
					enrollment.getCourse().getId()
				);
				progressSum += (completedLessons * 100.0) / totalLessons;
			}
			averageCourseProgress = progressSum / enrollments.size();
		}

		return new MetricsResponse(
			courseEnrollmentRepository.countByStudentIdAndStatusIn(student.getId(), ACTIVE_ENROLLMENTS),
			lessonProgressRepository.countByStudentIdAndCompletedTrue(student.getId()),
			round(averageCourseProgress),
			quizAttemptRepository.countDistinctQuizIdByStudentId(student.getId()),
			round(quizAttemptRepository.findAverageScoreByStudentId(student.getId())),
			commentRepository.countByStudentId(student.getId())
		);
	}

	private double round(Double value) {
		if (value == null) {
			return 0.0;
		}
		return Math.round(value * 100.0) / 100.0;
	}
}
