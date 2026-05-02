package com.devnest.course.service;

import com.devnest.auth.security.AuthenticatedUserProvider;
import com.devnest.course.domain.Course;
import com.devnest.course.domain.CourseModule;
import com.devnest.course.domain.Lesson;
import com.devnest.course.domain.Quiz;
import com.devnest.course.domain.QuizOption;
import com.devnest.course.domain.QuizQuestion;
import com.devnest.course.repository.CourseModuleRepository;
import com.devnest.course.repository.CourseRepository;
import com.devnest.course.repository.LessonRepository;
import com.devnest.course.repository.QuizOptionRepository;
import com.devnest.course.repository.QuizQuestionRepository;
import com.devnest.course.repository.QuizRepository;
import com.devnest.shared.exception.ForbiddenException;
import com.devnest.shared.exception.ResourceNotFoundException;
import com.devnest.user.domain.User;
import com.devnest.user.domain.UserRole;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseAuthoringAccessService {

	private final AuthenticatedUserProvider authenticatedUserProvider;
	private final CourseRepository courseRepository;
	private final CourseModuleRepository courseModuleRepository;
	private final LessonRepository lessonRepository;
	private final QuizRepository quizRepository;
	private final QuizQuestionRepository quizQuestionRepository;
	private final QuizOptionRepository quizOptionRepository;

	public User getAuthenticatedTeacher() {
		User teacher = authenticatedUserProvider.getAuthenticatedUser();

		if (teacher.getRole() != UserRole.TEACHER) {
			throw new ForbiddenException("Only teachers can manage courses.");
		}

		return teacher;
	}

	public Course getOwnedCourse(UUID courseId) {
		User teacher = getAuthenticatedTeacher();
		return courseRepository.findByIdAndTeacherId(courseId, teacher.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Course not found."));
	}

	public CourseModule getOwnedModule(UUID courseId, UUID moduleId) {
		Course course = getOwnedCourse(courseId);
		CourseModule module = courseModuleRepository.findById(moduleId)
			.orElseThrow(() -> new ResourceNotFoundException("Module not found."));

		if (!module.getCourse().getId().equals(course.getId())) {
			throw new ResourceNotFoundException("Module not found.");
		}

		return module;
	}

	public Lesson getOwnedLesson(UUID courseId, UUID moduleId, UUID lessonId) {
		CourseModule module = getOwnedModule(courseId, moduleId);
		Lesson lesson = lessonRepository.findById(lessonId)
			.orElseThrow(() -> new ResourceNotFoundException("Lesson not found."));

		if (!lesson.getModule().getId().equals(module.getId())) {
			throw new ResourceNotFoundException("Lesson not found.");
		}

		return lesson;
	}

	public Quiz getOwnedQuiz(UUID courseId, UUID moduleId, UUID lessonId) {
		Lesson lesson = getOwnedLesson(courseId, moduleId, lessonId);
		return quizRepository.findByLessonId(lesson.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));
	}

	public QuizQuestion getOwnedQuestion(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId) {
		Quiz quiz = getOwnedQuiz(courseId, moduleId, lessonId);
		QuizQuestion question = quizQuestionRepository.findById(questionId)
			.orElseThrow(() -> new ResourceNotFoundException("Question not found."));

		if (!question.getQuiz().getId().equals(quiz.getId())) {
			throw new ResourceNotFoundException("Question not found.");
		}

		return question;
	}

	public QuizOption getOwnedOption(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, UUID optionId) {
		QuizQuestion question = getOwnedQuestion(courseId, moduleId, lessonId, questionId);
		QuizOption option = quizOptionRepository.findById(optionId)
			.orElseThrow(() -> new ResourceNotFoundException("Option not found."));

		if (!option.getQuestion().getId().equals(question.getId())) {
			throw new ResourceNotFoundException("Option not found.");
		}

		return option;
	}
}
