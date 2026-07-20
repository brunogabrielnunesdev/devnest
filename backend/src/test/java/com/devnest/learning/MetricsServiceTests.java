package com.devnest.learning;

import static org.assertj.core.api.Assertions.assertThat;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.course.dto.course.CourseCreateRequest;
import com.devnest.course.dto.module.ModuleCreateRequest;
import com.devnest.course.dto.comment.CommentCreateRequest;
import com.devnest.course.dto.lesson.LessonCreateRequest;
import com.devnest.course.dto.option.OptionCreateRequest;
import com.devnest.course.dto.question.QuestionCreateRequest;
import com.devnest.course.dto.quiz.attempt.AttemptAnswerRequest;
import com.devnest.course.dto.quiz.attempt.AttemptSubmitRequest;
import com.devnest.course.dto.quiz.QuizCreateRequest;
import com.devnest.course.mapper.course.CourseMapper;
import com.devnest.course.mapper.lesson.LessonMapper;
import com.devnest.course.mapper.module.ModuleMapper;
import com.devnest.course.mapper.option.OptionMapper;
import com.devnest.course.mapper.question.QuestionMapper;
import com.devnest.course.mapper.quiz.QuizMapper;
import com.devnest.course.service.course.CourseEnrollmentService;
import com.devnest.course.service.module.ModuleService;
import com.devnest.course.service.course.CourseService;
import com.devnest.course.service.comment.CommentService;
import com.devnest.course.service.lesson.LessonProgressService;
import com.devnest.course.service.lesson.LessonService;
import com.devnest.course.service.option.OptionService;
import com.devnest.course.service.question.QuestionService;
import com.devnest.course.service.quiz.QuizService;
import com.devnest.course.service.student.metrics.StudentMetricsService;
import com.devnest.course.service.student.StudentQuizService;
import com.devnest.course.service.teacher.TeacherMetricsService;
import com.devnest.identity.entity.User;
import com.devnest.identity.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class MetricsServiceTests {

	@Autowired
	private TeacherMetricsService teacherMetricsService;

	@Autowired
	private StudentMetricsService studentMetricsService;

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
	private CourseEnrollmentService courseEnrollmentService;

	@Autowired
	private LessonProgressService lessonProgressService;

	@Autowired
	private StudentQuizService studentQuizService;

	@Autowired
	private CommentService commentService;

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
	void teacherMetricsReturnOnlyAuthenticatedTeacherData() {
		User teacher = saveTeacher("teacher-metrics@example.com", "Teacher Metrics");
		User studentOne = saveStudent("teacher-metrics-student-one@example.com", "Student One");
		User studentTwo = saveStudent("teacher-metrics-student-two@example.com", "Student Two");
		TeacherFixture fixture = createTeacherFixture(teacher, "teacher-metrics");

		authenticate(studentOne);
		courseEnrollmentService.enroll(fixture.firstCourseId());
		studentQuizService.submitAttempt(
			fixture.firstCourseId(),
			fixture.firstCourseFirstLessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(fixture.firstQuizQuestionId(), fixture.firstQuizCorrectOptionId())
			))
		);
		lessonProgressService.completeLesson(fixture.firstCourseId(), fixture.firstCourseFirstLessonId());
		commentService.create(
			fixture.firstCourseId(),
			fixture.firstCourseFirstLessonId(),
			new CommentCreateRequest("Great first lesson", 8).content(),
			8
		);

		authenticate(studentTwo);
		courseEnrollmentService.enroll(fixture.firstCourseId());
		courseEnrollmentService.enroll(fixture.secondCourseId());
		studentQuizService.submitAttempt(
			fixture.firstCourseId(),
			fixture.firstCourseSecondLessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(fixture.secondQuizQuestionId(), fixture.secondQuizCorrectOptionId())
			))
		);
		lessonProgressService.completeLesson(fixture.firstCourseId(), fixture.firstCourseSecondLessonId());
		commentService.create(
			fixture.firstCourseId(),
			fixture.firstCourseSecondLessonId(),
			new CommentCreateRequest("Loved this one too", 6).content(),
			6
		);
		lessonProgressService.completeLesson(fixture.secondCourseId(), fixture.secondCourseLessonId());
		commentService.create(
			fixture.secondCourseId(),
			fixture.secondCourseLessonId(),
			new CommentCreateRequest("Solid practical lesson", 10).content(),
			10
		);

		authenticate(teacher);
		var metrics = teacherMetricsService.getMetrics();

		assertThat(metrics.totalCoursesCreated()).isEqualTo(2);
		assertThat(metrics.totalModules()).isEqualTo(2);
		assertThat(metrics.totalLessons()).isEqualTo(3);
		assertThat(metrics.totalStudentsEnrolled()).isEqualTo(3);
		assertThat(metrics.averageCourseRating()).isEqualTo(8.0);
		assertThat(metrics.totalCommentsReceived()).isEqualTo(3);
		assertThat(metrics.totalQuizzesCreated()).isEqualTo(2);
	}

	@Test
	void studentMetricsReturnOnlyAuthenticatedStudentData() {
		User teacher = saveTeacher("student-metrics-teacher@example.com", "Teacher Metrics");
		User student = saveStudent("student-metrics@example.com", "Student Metrics");
		StudentFixture fixture = createStudentFixture(teacher, "student-metrics");

		authenticate(student);
		courseEnrollmentService.enroll(fixture.firstCourseId());
		courseEnrollmentService.enroll(fixture.secondCourseId());

		studentQuizService.submitAttempt(
			fixture.firstCourseId(),
			fixture.firstCourseLessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(fixture.firstQuizQuestionId(), fixture.firstQuizCorrectOptionId()),
				new AttemptAnswerRequest(fixture.secondQuizQuestionId(), fixture.secondQuizWrongOptionId())
			))
		);
		lessonProgressService.completeLesson(fixture.firstCourseId(), fixture.firstCourseLessonId());
		commentService.create(
			fixture.firstCourseId(),
			fixture.firstCourseLessonId(),
			new CommentCreateRequest("Nice lesson", 7).content(),
			7
		);

		studentQuizService.submitAttempt(
			fixture.secondCourseId(),
			fixture.secondCourseLessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(fixture.thirdQuizQuestionId(), fixture.thirdQuizCorrectOptionId())
			))
		);
		lessonProgressService.completeLesson(fixture.secondCourseId(), fixture.secondCourseLessonId());
		commentService.create(
			fixture.secondCourseId(),
			fixture.secondCourseLessonId(),
			new CommentCreateRequest("Very objective", 9).content(),
			9
		);

		var metrics = studentMetricsService.getMetrics();

		assertThat(metrics.totalCoursesEnrolled()).isEqualTo(2);
		assertThat(metrics.totalLessonsCompleted()).isEqualTo(2);
		assertThat(metrics.averageCourseProgress()).isEqualTo(75.0);
		assertThat(metrics.totalQuizzesCompleted()).isEqualTo(2);
		assertThat(metrics.averageQuizAccuracy()).isEqualTo(75.0);
		assertThat(metrics.totalCommentsMade()).isEqualTo(2);
	}

	private TeacherFixture createTeacherFixture(User teacher, String prefix) {
		authenticate(teacher);
		var firstCourse = courseService.create(courseMapper.toEntity(
			new CourseCreateRequest("Course " + prefix + " A", "Description", "BEGINNER")
		));
		var firstModule = moduleService.create(firstCourse.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module A", "Description", 1)
		));
		var firstLesson = lessonService.create(firstCourse.id(), firstModule.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson A1", "Description", "content", "https://video", 1)
		));
		var secondLesson = lessonService.create(firstCourse.id(), firstModule.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson A2", "Description", "content", "https://video", 2)
		));

		var firstQuiz = quizService.create(firstCourse.id(), firstModule.id(), firstLesson.id(), quizMapper.toEntity(
			new QuizCreateRequest("Quiz A1", 100, 3, 1)
		));
		var firstQuestion = questionService.create(firstCourse.id(), firstModule.id(), firstLesson.id(), questionMapper.toEntity(
			new QuestionCreateRequest("Question A1", 1)
		));
		var firstCorrectOption = optionService.create(firstCourse.id(), firstModule.id(), firstLesson.id(), firstQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Correct A1", true, 1)
		));
		optionService.create(firstCourse.id(), firstModule.id(), firstLesson.id(), firstQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Wrong A1", false, 2)
		));

		var secondQuiz = quizService.create(firstCourse.id(), firstModule.id(), secondLesson.id(), quizMapper.toEntity(
			new QuizCreateRequest("Quiz A2", 100, 3, 1)
		));
		var secondQuestion = questionService.create(firstCourse.id(), firstModule.id(), secondLesson.id(), questionMapper.toEntity(
			new QuestionCreateRequest("Question A2", 1)
		));
		var secondCorrectOption = optionService.create(firstCourse.id(), firstModule.id(), secondLesson.id(), secondQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Correct A2", true, 1)
		));
		optionService.create(firstCourse.id(), firstModule.id(), secondLesson.id(), secondQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Wrong A2", false, 2)
		));
		courseService.publish(firstCourse.id());

		var secondCourse = courseService.create(courseMapper.toEntity(
			new CourseCreateRequest("Course " + prefix + " B", "Description", "BEGINNER")
		));
		var secondModule = moduleService.create(secondCourse.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module B", "Description", 1)
		));
		var secondCourseLesson = lessonService.create(secondCourse.id(), secondModule.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson B1", "Description", "content", "https://video", 1)
		));
		courseService.publish(secondCourse.id());

		return new TeacherFixture(
			firstCourse.id(),
			secondCourse.id(),
			firstLesson.id(),
			secondLesson.id(),
			secondCourseLesson.id(),
			firstQuiz.id(),
			secondQuiz.id(),
			firstQuestion.id(),
			secondQuestion.id(),
			firstCorrectOption.id(),
			secondCorrectOption.id()
		);
	}

	private StudentFixture createStudentFixture(User teacher, String prefix) {
		authenticate(teacher);
		var firstCourse = courseService.create(courseMapper.toEntity(
			new CourseCreateRequest("Course " + prefix + " A", "Description", "BEGINNER")
		));
		var firstModule = moduleService.create(firstCourse.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module A", "Description", 1)
		));
		var firstLesson = lessonService.create(firstCourse.id(), firstModule.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson A1", "Description", "content", "https://video", 1)
		));
		lessonService.create(firstCourse.id(), firstModule.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson A2", "Description", "content", "https://video", 2)
		));
		quizService.create(firstCourse.id(), firstModule.id(), firstLesson.id(), quizMapper.toEntity(
			new QuizCreateRequest("Quiz A1", 50, 3, 2)
		));
		var firstQuestion = questionService.create(firstCourse.id(), firstModule.id(), firstLesson.id(), questionMapper.toEntity(
			new QuestionCreateRequest("Question A1", 1)
		));
		var secondQuestion = questionService.create(firstCourse.id(), firstModule.id(), firstLesson.id(), questionMapper.toEntity(
			new QuestionCreateRequest("Question A2", 2)
		));
		var firstCorrectOption = optionService.create(firstCourse.id(), firstModule.id(), firstLesson.id(), firstQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Correct A1", true, 1)
		));
		optionService.create(firstCourse.id(), firstModule.id(), firstLesson.id(), firstQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Wrong A1", false, 2)
		));
		optionService.create(firstCourse.id(), firstModule.id(), firstLesson.id(), secondQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Correct A2", true, 1)
		));
		var secondWrongOption = optionService.create(firstCourse.id(), firstModule.id(), firstLesson.id(), secondQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Wrong A2", false, 2)
		));
		courseService.publish(firstCourse.id());

		var secondCourse = courseService.create(courseMapper.toEntity(
			new CourseCreateRequest("Course " + prefix + " B", "Description", "BEGINNER")
		));
		var secondModule = moduleService.create(secondCourse.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module B", "Description", 1)
		));
		var secondLesson = lessonService.create(secondCourse.id(), secondModule.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson B1", "Description", "content", "https://video", 1)
		));
		quizService.create(secondCourse.id(), secondModule.id(), secondLesson.id(), quizMapper.toEntity(
			new QuizCreateRequest("Quiz B1", 100, 3, 1)
		));
		var thirdQuestion = questionService.create(secondCourse.id(), secondModule.id(), secondLesson.id(), questionMapper.toEntity(
			new QuestionCreateRequest("Question B1", 1)
		));
		var thirdCorrectOption = optionService.create(secondCourse.id(), secondModule.id(), secondLesson.id(), thirdQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Correct B1", true, 1)
		));
		optionService.create(secondCourse.id(), secondModule.id(), secondLesson.id(), thirdQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Wrong B1", false, 2)
		));
		courseService.publish(secondCourse.id());

		return new StudentFixture(
			firstCourse.id(),
			secondCourse.id(),
			firstLesson.id(),
			secondLesson.id(),
			firstQuestion.id(),
			secondQuestion.id(),
			thirdQuestion.id(),
			firstCorrectOption.id(),
			secondWrongOption.id(),
			thirdCorrectOption.id()
		);
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

	private User saveTeacher(String email, String displayName) {
		return userRepository.save(User.createTeacher(email, "password-hash", displayName));
	}

	private User saveStudent(String email, String displayName) {
		return userRepository.save(User.createStudent(email, "password-hash", displayName));
	}

	private record TeacherFixture(
		UUID firstCourseId,
		UUID secondCourseId,
		UUID firstCourseFirstLessonId,
		UUID firstCourseSecondLessonId,
		UUID secondCourseLessonId,
		UUID firstQuizId,
		UUID secondQuizId,
		UUID firstQuizQuestionId,
		UUID secondQuizQuestionId,
		UUID firstQuizCorrectOptionId,
		UUID secondQuizCorrectOptionId
	) {
	}

	private record StudentFixture(
		UUID firstCourseId,
		UUID secondCourseId,
		UUID firstCourseLessonId,
		UUID secondCourseLessonId,
		UUID firstQuizQuestionId,
		UUID secondQuizQuestionId,
		UUID thirdQuizQuestionId,
		UUID firstQuizCorrectOptionId,
		UUID secondQuizWrongOptionId,
		UUID thirdQuizCorrectOptionId
	) {
	}
}
