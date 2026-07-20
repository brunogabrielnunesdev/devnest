package com.devnest.admin.service.course;

import com.devnest.admin.dto.course.CourseResponse;
import com.devnest.admin.dto.adminpage.AdminPageResponse;
import com.devnest.admin.service.acess.AccessService;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.course.entity.course.Course;
import com.devnest.course.repository.course.CourseEnrollmentRepository;
import com.devnest.course.repository.module.ModuleRepository;
import com.devnest.course.repository.course.CourseRepository;
import com.devnest.course.repository.comment.CommentRepository;
import com.devnest.course.repository.lesson.LessonProgressRepository;
import com.devnest.course.repository.lesson.LessonRepository;
import com.devnest.course.repository.quiz.QuizAnswerRepository;
import com.devnest.course.repository.quiz.QuizAttemptRepository;
import com.devnest.course.repository.option.OptionRepository;
import com.devnest.course.repository.question.QuestionRepository;
import com.devnest.course.repository.quiz.QuizRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminCourseService")
@RequiredArgsConstructor
public class CourseService {

	private final AccessService accessService;
	private final CourseRepository courseRepository;
	private final CourseEnrollmentRepository courseEnrollmentRepository;
	private final ModuleRepository moduleRepository;
	private final LessonRepository lessonRepository;
	private final CommentRepository commentRepository;
	private final LessonProgressRepository lessonProgressRepository;
	private final QuizRepository quizRepository;
	private final QuestionRepository questionRepository;
	private final OptionRepository optionRepository;
	private final QuizAttemptRepository quizAttemptRepository;
	private final QuizAnswerRepository quizAnswerRepository;

	@Transactional(readOnly = true)
	public AdminPageResponse<CourseResponse> findAll(String query, int page, int size) {
		accessService.getAuthenticatedAdmin();

		var coursePage = courseRepository.findAdminCourses(normalizeQuery(query), PageRequest.of(page, size));
		return new AdminPageResponse<>(
			coursePage.getContent().stream().map(this::toResponse).toList(),
			coursePage.getNumber(),
			coursePage.getSize(),
			coursePage.getTotalElements(),
			coursePage.getTotalPages()
		);
	}

	@Transactional(readOnly = true)
	public java.util.List<CourseResponse> findAllList(String query) {
		accessService.getAuthenticatedAdmin();
		String normalizedQuery = normalizeQuery(query);

		return courseRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
			.stream()
			.filter(course -> matchesQuery(course, normalizedQuery))
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public CourseResponse findById(UUID courseId) {
		accessService.getAuthenticatedAdmin();
		return toResponse(findCourse(courseId));
	}

	@Transactional
	public CourseResponse archive(UUID courseId) {
		accessService.getAuthenticatedAdmin();
		Course course = findCourse(courseId);
		course.archive();
		return toResponse(course);
	}

	@Transactional
	public CourseResponse restore(UUID courseId) {
		accessService.getAuthenticatedAdmin();
		Course course = findCourse(courseId);
		course.restore();
		return toResponse(course);
	}

	@Transactional
	public void delete(UUID courseId) {
		accessService.getAuthenticatedAdmin();
		Course course = findCourse(courseId);
		quizAnswerRepository.deleteAllByAttemptQuizLessonModuleCourseId(courseId);
		quizAttemptRepository.deleteAllByQuizLessonModuleCourseId(courseId);
		commentRepository.deleteAllByLessonModuleCourseId(courseId);
		lessonProgressRepository.deleteAllByLessonModuleCourseId(courseId);
		optionRepository.deleteAllByQuestionQuizLessonModuleCourseId(courseId);
		questionRepository.deleteAllByQuizLessonModuleCourseId(courseId);
		quizRepository.deleteAllByLessonModuleCourseId(courseId);
		lessonRepository.deleteAllByModuleCourseId(courseId);
		courseEnrollmentRepository.deleteAllByCourseId(courseId);
		moduleRepository.deleteAllByCourseId(courseId);
		courseRepository.delete(course);
	}

	private Course findCourse(UUID courseId) {
		return courseRepository.findById(courseId)
			.orElseThrow(() -> new ResourceNotFoundException("Course not found."));
	}

	private String normalizeQuery(String query) {
		if (query == null || query.isBlank()) {
			return null;
		}
		return query.trim().toLowerCase();
	}

	private boolean matchesQuery(Course course, String query) {
		if (query == null) {
			return true;
		}

		String title = course.getTitle() != null ? course.getTitle().toLowerCase() : "";
		return title.contains(query);
	}

	private CourseResponse toResponse(Course course) {
		return new CourseResponse(
			course.getId(),
			course.getTeacher().getId(),
			course.getTeacher().getProfile().getDisplayName(),
			course.getTitle(),
			course.getDescription(),
			course.getLevel(),
			course.getCoverImageUrl(),
			course.getStatus(),
			course.isArchived(),
			course.getCreatedAt(),
			course.getUpdatedAt()
		);
	}
}
