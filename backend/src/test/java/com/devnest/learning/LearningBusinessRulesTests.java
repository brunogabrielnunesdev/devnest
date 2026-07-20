package com.devnest.learning;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.common.exception.ConflictException;
import com.devnest.course.dto.module.ModuleCreateRequest;
import com.devnest.course.dto.option.OptionCreateRequest;
import com.devnest.course.mapper.course.CourseMapper;
import com.devnest.course.mapper.lesson.LessonMapper;
import com.devnest.course.mapper.module.ModuleMapper;
import com.devnest.course.mapper.option.OptionMapper;
import com.devnest.course.mapper.question.QuestionMapper;
import com.devnest.course.mapper.quiz.QuizMapper;
import com.devnest.identity.entity.User;
import com.devnest.identity.repository.UserRepository;
import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.course.dto.course.CourseCreateRequest;
import com.devnest.course.dto.lesson.LessonCreateRequest;
import com.devnest.course.dto.quiz.QuizCreateRequest;
import com.devnest.course.dto.question.QuestionCreateRequest;
import com.devnest.course.service.module.ModuleService;
import com.devnest.course.service.course.CourseService;
import com.devnest.course.service.lesson.LessonService;
import com.devnest.course.service.option.OptionService;
import com.devnest.course.service.question.QuestionService;
import com.devnest.course.service.quiz.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class LearningBusinessRulesTests {

	@Autowired
	private CourseService courseService;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private LessonService lessonService;

	@Autowired
	private QuizService quizService;

	@Autowired
	private QuestionService questionService;

	@Autowired
	private OptionService optionService;

	@Autowired
	private CourseMapper courseMapper;

	@Autowired
	private ModuleMapper moduleMapper;

	@Autowired
	private LessonMapper lessonMapper;

	@Autowired
	private QuizMapper quizMapper;

	@Autowired
	private QuestionMapper questionMapper;

	@Autowired
	private OptionMapper optionMapper;

	@Autowired
	private UserRepository userRepository;

	@Test
	void modulePositionMustBeUniqueInsideCourse() {
		User teacher = saveTeacher("module-rules@example.com");
		authenticate(teacher);
		var course = courseService.create(courseMapper.toEntity(new CourseCreateRequest("Course", "Description", "BEGINNER")));

		moduleService.create(course.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module 1", "Description", 1)
		));

		assertThatThrownBy(() -> moduleService.create(
			course.id(),
			moduleMapper.toEntity(new ModuleCreateRequest("Module 2", "Description", 1))
		)).isInstanceOf(ConflictException.class)
			.hasMessage("Module position is already in use for this course.");
	}

	@Test
	void lessonPositionMustBeUniqueInsideModule() {
		User teacher = saveTeacher("lesson-rules@example.com");
		authenticate(teacher);
		var course = courseService.create(courseMapper.toEntity(new CourseCreateRequest("Course", "Description", "BEGINNER")));
		var module = moduleService.create(course.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module", "Description", 1)
		));

		lessonService.create(course.id(), module.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson 1", "Description", "content", "https://video", 1)
		));

		assertThatThrownBy(() -> lessonService.create(
			course.id(),
			module.id(),
			lessonMapper.toEntity(new LessonCreateRequest("Lesson 2", "Description", "content", "https://video", 1))
		)).isInstanceOf(ConflictException.class)
			.hasMessage("Lesson position is already in use for this module.");
	}

	@Test
	void quizCannotExceedMaximumNumberOfQuestions() {
		User teacher = saveTeacher("quiz-question-rules@example.com");
		authenticate(teacher);
		var course = courseService.create(courseMapper.toEntity(new CourseCreateRequest("Course", "Description", "BEGINNER")));
		var module = moduleService.create(course.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module", "Description", 1)
		));
		var lesson = lessonService.create(course.id(), module.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson", "Description", "content", "https://video", 1)
		));
		quizService.create(course.id(), module.id(), lesson.id(), quizMapper.toEntity(
			new QuizCreateRequest("Quiz", 70, 3, 1)
		));

		questionService.create(course.id(), module.id(), lesson.id(), questionMapper.toEntity(
			new QuestionCreateRequest("Question 1", 1)
		));

		assertThatThrownBy(() -> questionService.create(
			course.id(),
			module.id(),
			lesson.id(),
			questionMapper.toEntity(new QuestionCreateRequest("Question 2", 1))
		)).isInstanceOf(ConflictException.class)
			.hasMessage("Quiz already reached the maximum number of questions.");
	}

	@Test
	void optionPositionMustBeUniqueInsideQuestion() {
		User teacher = saveTeacher("quiz-option-rules@example.com");
		authenticate(teacher);
		var course = courseService.create(courseMapper.toEntity(new CourseCreateRequest("Course", "Description", "BEGINNER")));
		var module = moduleService.create(course.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module", "Description", 1)
		));
		var lesson = lessonService.create(course.id(), module.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson", "Description", "content", "https://video", 1)
		));
		quizService.create(course.id(), module.id(), lesson.id(), quizMapper.toEntity(
			new QuizCreateRequest("Quiz", 70, 3, 10)
		));
		var question = questionService.create(course.id(), module.id(), lesson.id(), questionMapper.toEntity(
			new QuestionCreateRequest("Question 1", 1)
		));

		optionService.create(course.id(), module.id(), lesson.id(), question.id(), optionMapper.toEntity(
			new OptionCreateRequest("Option 1", true, 1)
		));

		assertThatThrownBy(() -> optionService.create(
			course.id(),
			module.id(),
			lesson.id(),
			question.id(),
			optionMapper.toEntity(new OptionCreateRequest("Option 2", false, 1))
		)).isInstanceOf(ConflictException.class)
			.hasMessage("Option position is already in use for this question.");
	}

	private void authenticate(User user) {
		CustomAuthentication customAuthentication = new CustomAuthentication(user);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			customAuthentication,
			null,
			customAuthentication.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private User saveTeacher(String email) {
		return userRepository.save(User.createTeacher(email, "password-hash", "Teacher"));
	}
}

