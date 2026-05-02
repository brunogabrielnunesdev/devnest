package com.devnest.course;

import static org.assertj.core.api.Assertions.assertThat;

import com.devnest.auth.security.AuthenticatedUser;
import com.devnest.course.domain.Quiz;
import com.devnest.course.dto.CourseCreateRequest;
import com.devnest.course.dto.CourseModuleCreateRequest;
import com.devnest.course.dto.LessonCreateRequest;
import com.devnest.course.dto.QuizCreateRequest;
import com.devnest.course.mapper.CourseMapper;
import com.devnest.course.mapper.CourseModuleMapper;
import com.devnest.course.mapper.LessonMapper;
import com.devnest.course.mapper.QuizMapper;
import com.devnest.course.service.CourseModuleService;
import com.devnest.course.service.CourseService;
import com.devnest.course.service.LessonService;
import com.devnest.course.service.QuizService;
import com.devnest.user.domain.User;
import com.devnest.user.repository.UserRepository;
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
	private CourseModuleService courseModuleService;

	@Autowired
	private LessonService lessonService;

	@Autowired
	private QuizService quizService;

	@Autowired
	private CourseMapper courseMapper;

	@Autowired
	private CourseModuleMapper courseModuleMapper;

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

		var module = courseModuleService.create(course.id(), courseModuleMapper.toEntity(
			new CourseModuleCreateRequest("Module", "Module description", 1)
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
		var authenticatedUser = new AuthenticatedUser(user);
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
