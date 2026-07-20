package com.devnest.course.service.student;

import com.devnest.common.exception.ForbiddenException;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import com.devnest.auth.security.useridentity.CustomUserProvider;
import com.devnest.course.entity.course.Course;
import com.devnest.course.entity.course.CourseStatus;
import com.devnest.course.entity.lesson.Lesson;
import com.devnest.course.entity.quiz.Quiz;
import com.devnest.course.repository.course.CourseRepository;
import com.devnest.course.repository.lesson.LessonRepository;
import com.devnest.course.repository.quiz.QuizRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentLearningAccessService {

	private final CustomUserProvider customUserProvider;
	private final CourseRepository courseRepository;
	private final LessonRepository lessonRepository;
	private final QuizRepository quizRepository;

	public User getAuthenticatedStudent() {
		User student = customUserProvider.getAuthenticatedUser();

		if (student.getRole() != UserRole.STUDENT) {
			throw new ForbiddenException("Only students can access this feature.");
		}

		return student;
	}

	public Course getPublishedCourse(UUID courseId) {
		return courseRepository.findByIdAndStatusAndArchivedFalse(courseId, CourseStatus.PUBLISHED)
			.orElseThrow(() -> new ResourceNotFoundException("Course not found."));
	}

	public Lesson getPublishedCourseLesson(UUID courseId, UUID lessonId) {
		Course course = getPublishedCourse(courseId);
		Lesson lesson = lessonRepository.findById(lessonId)
			.orElseThrow(() -> new ResourceNotFoundException("Lesson not found."));

		if (!lesson.getModule().getCourse().getId().equals(course.getId())) {
			throw new ResourceNotFoundException("Lesson not found.");
		}

		return lesson;
	}

	public Quiz getPublishedCourseQuiz(UUID courseId, UUID lessonId) {
		Lesson lesson = getPublishedCourseLesson(courseId, lessonId);
		return quizRepository.findByLessonId(lesson.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));
	}
}

