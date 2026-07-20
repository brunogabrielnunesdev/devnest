package com.devnest.learning;

import static org.assertj.core.api.Assertions.assertThat;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.course.dto.course.CourseCreateRequest;
import com.devnest.course.dto.module.ModuleCreateRequest;
import com.devnest.course.dto.lesson.LessonCreateRequest;
import com.devnest.course.dto.quiz.QuizCreateRequest;
import com.devnest.course.mapper.course.CourseMapper;
import com.devnest.course.mapper.lesson.LessonMapper;
import com.devnest.course.mapper.module.ModuleMapper;
import com.devnest.course.mapper.quiz.QuizMapper;
import com.devnest.course.service.module.ModuleService;
import com.devnest.course.service.course.CourseService;
import com.devnest.course.service.lesson.LessonService;
import com.devnest.course.service.quiz.QuizService;
import com.devnest.identity.entity.User;
import com.devnest.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class CourseAuthoringCrudTests {

	@Autowired
	private CourseService courseService;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private LessonService lessonService;

	@Autowired
	private QuizService quizService;

	@Autowired
	private CourseMapper courseMapper;

	@Autowired
	private ModuleMapper moduleMapper;

	@Autowired
	private LessonMapper lessonMapper;

	@Autowired
	private QuizMapper quizMapper;

	@Autowired
	private UserRepository userRepository;

	@Test
	void teacherCanCreateModuleLessonAndQuizInsideOwnedCourse() {
		var teacher = saveTeacher("teacher-authoring@example.com");
		authenticate(teacher);

		var course = courseService.create(courseMapper.toEntity(
			new CourseCreateRequest("Course", "Description", "BEGINNER")
		));

		var module = moduleService.create(course.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module", "Module description", 1)
		));

		var lesson = lessonService.create(course.id(), module.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson", "Lesson description", "content", "https://video", 1)
		));

		var quiz = quizService.create(course.id(), module.id(), lesson.id(), quizMapper.toEntity(
			new QuizCreateRequest("Quiz", 70, 3, 10)
		));

		assertThat(module.courseId()).isEqualTo(course.id());
		assertThat(lesson.moduleId()).isEqualTo(module.id());
		assertThat(quiz.lessonId()).isEqualTo(lesson.id());
		assertThat(quiz.title()).isEqualTo("Quiz");
	}

	private void authenticate(User user) {
		var authenticatedUser = new CustomAuthentication(user);
		var authentication = new UsernamePasswordAuthenticationToken(
			authenticatedUser,
			null,
			authenticatedUser.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private User saveTeacher(String email) {
		return userRepository.save(User.createTeacher(email, "password-hash", "Teacher"));
	}
}

