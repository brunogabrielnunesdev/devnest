package com.devnest.course.service.course;

import com.devnest.auth.security.useridentity.CustomUserProvider;
import com.devnest.course.entity.module.Module;
import com.devnest.course.entity.course.Course;
import com.devnest.course.entity.lesson.Lesson;
import com.devnest.course.entity.quiz.Quiz;
import com.devnest.course.entity.quiz.option.Option;
import com.devnest.course.entity.quiz.QuizQuestion;
import com.devnest.course.repository.module.ModuleRepository;
import com.devnest.course.repository.course.CourseRepository;
import com.devnest.course.repository.lesson.LessonRepository;
import com.devnest.course.repository.option.OptionRepository;
import com.devnest.course.repository.question.QuestionRepository;
import com.devnest.course.repository.quiz.QuizRepository;
import com.devnest.common.exception.ForbiddenException;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseAuthoringAccessService {

	private final CustomUserProvider customUserProvider;
	private final CourseRepository courseRepository;
	private final ModuleRepository moduleRepository;
	private final LessonRepository lessonRepository;
	private final QuizRepository quizRepository;
	private final QuestionRepository questionRepository;
	private final OptionRepository optionRepository;


	public User getAuthenticatedTeacher() {
		User teacher = customUserProvider.getAuthenticatedUser();

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

	public Module getOwnedModule(UUID courseId, UUID moduleId) {
		Course course = getOwnedCourse(courseId);
		Module module = moduleRepository.findById(moduleId)
			.orElseThrow(() -> new ResourceNotFoundException("Module not found."));

		if (!module.getCourse().getId().equals(course.getId())) {
			throw new ResourceNotFoundException("Module not found.");
		}

		return module;
	}

	public Lesson getOwnedLesson(UUID courseId, UUID moduleId, UUID lessonId) {
		Module module = getOwnedModule(courseId, moduleId);
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
		QuizQuestion question = questionRepository.findById(questionId)
			.orElseThrow(() -> new ResourceNotFoundException("Question not found."));

		if (!question.getQuiz().getId().equals(quiz.getId())) {
			throw new ResourceNotFoundException("Question not found.");
		}

		return question;
	}

	public Option getOwnedOption(UUID courseId, UUID moduleId, UUID lessonId, UUID questionId, UUID optionId) {
		QuizQuestion question = getOwnedQuestion(courseId, moduleId, lessonId, questionId);
		Option option = optionRepository.findById(optionId)
			.orElseThrow(() -> new ResourceNotFoundException("Option not found."));

		if (!option.getQuestion().getId().equals(question.getId())) {
			throw new ResourceNotFoundException("Option not found.");
		}

		return option;
	}
}
