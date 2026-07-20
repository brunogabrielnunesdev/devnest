package com.devnest.course.service.teacher;

import com.devnest.course.dto.teacher.TeacherMetricsResponse;
import com.devnest.course.entity.course.EnrollmentStatus;
import com.devnest.course.repository.course.CourseEnrollmentRepository;
import com.devnest.course.repository.module.ModuleRepository;
import com.devnest.course.repository.course.CourseRepository;
import com.devnest.course.repository.comment.CommentRepository;
import com.devnest.course.repository.lesson.LessonRepository;
import com.devnest.course.repository.quiz.QuizRepository;
import java.util.List;

import com.devnest.course.service.course.CourseAuthoringAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherMetricsService {

	private static final List<EnrollmentStatus> ACTIVE_ENROLLMENTS = List.of(
		EnrollmentStatus.ACTIVE,
		EnrollmentStatus.COMPLETED
	);

	private final CourseAuthoringAccessService courseAuthoringAccessService;
	private final CourseRepository courseRepository;
	private final ModuleRepository moduleRepository;
	private final LessonRepository lessonRepository;
	private final CourseEnrollmentRepository courseEnrollmentRepository;
	private final CommentRepository commentRepository;
	private final QuizRepository quizRepository;

	@Transactional(readOnly = true)
	public TeacherMetricsResponse getMetrics() {
		var teacher = courseAuthoringAccessService.getAuthenticatedTeacher();

		return new TeacherMetricsResponse(
			courseRepository.countByTeacherId(teacher.getId()),
			moduleRepository.countByCourseTeacherId(teacher.getId()),
			lessonRepository.countByModuleCourseTeacherId(teacher.getId()),
			courseEnrollmentRepository.countByCourseTeacherIdAndStatusIn(teacher.getId(), ACTIVE_ENROLLMENTS),
			round(commentRepository.findAverageRatingByTeacherId(teacher.getId())),
			commentRepository.countByLessonModuleCourseTeacherId(teacher.getId()),
			quizRepository.countByLessonModuleCourseTeacherId(teacher.getId())
		);
	}

	private double round(Double value) {
		if (value == null) {
			return 0.0;
		}
		return Math.round(value * 100.0) / 100.0;
	}
}
