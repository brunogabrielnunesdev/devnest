package com.devnest.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.common.exception.ConflictException;
import com.devnest.common.exception.ForbiddenException;
import com.devnest.course.dto.option.OptionCreateRequest;
import com.devnest.course.dto.question.QuestionCreateRequest;
import com.devnest.course.mapper.option.OptionMapper;
import com.devnest.identity.entity.User;
import com.devnest.identity.repository.UserRepository;
import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.course.dto.course.CourseCreateRequest;
import com.devnest.course.dto.module.ModuleCreateRequest;
import com.devnest.course.dto.lesson.LessonCreateRequest;
import com.devnest.course.dto.quiz.attempt.AttemptAnswerRequest;
import com.devnest.course.dto.quiz.attempt.AttemptSubmitRequest;
import com.devnest.course.dto.quiz.QuizCreateRequest;
import com.devnest.course.mapper.course.CourseMapper;
import com.devnest.course.mapper.module.ModuleMapper;
import com.devnest.course.mapper.lesson.LessonMapper;
import com.devnest.course.mapper.quiz.QuizMapper;
import com.devnest.course.mapper.question.QuestionMapper;
import com.devnest.course.service.course.CourseEnrollmentService;
import com.devnest.course.service.module.ModuleService;
import com.devnest.course.service.course.CourseService;
import com.devnest.course.service.lesson.LessonService;
import com.devnest.course.service.option.OptionService;
import com.devnest.course.service.question.QuestionService;
import com.devnest.course.service.quiz.QuizService;
import com.devnest.course.service.student.StudentQuizService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class StudentQuizFlowTests {

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
	private StudentQuizService studentQuizService;

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
	void enrolledStudentCanLoadQuizWithoutCorrectAnswers() {
		QuizFixture fixture = createPublishedQuizFixture("load-quiz", 70, 3);
		User student = saveStudent("student-load-quiz@example.com");
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());

		var response = studentQuizService.getQuiz(fixture.courseId(), fixture.lessonId());

		assertThat(response.id()).isEqualTo(fixture.quizId());
		assertThat(response.questions()).hasSize(2);
		assertThat(response.questions().get(0).options()).hasSize(2);
	}

	@Test
	void enrolledStudentCanSubmitAttemptAndReceiveScore() {
		QuizFixture fixture = createPublishedQuizFixture("submit-attempt", 70, 3);
		User student = saveStudent("student-submit-quiz@example.com");
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());

		var response = studentQuizService.submitAttempt(
			fixture.courseId(),
			fixture.lessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(fixture.firstQuestionId(), fixture.firstCorrectOptionId()),
				new AttemptAnswerRequest(fixture.secondQuestionId(), fixture.secondWrongOptionId())
			))
		);

		assertThat(response.attemptNumber()).isEqualTo(1);
		assertThat(response.score()).isEqualTo(50);
		assertThat(response.passed()).isFalse();
		assertThat(response.remainingAttempts()).isEqualTo(2);
		assertThat(response.reviewAvailable()).isFalse();
		assertThat(response.answers()).isNull();
	}

	@Test
	void studentReceivesDetailedReviewWhenPassingQuiz() {
		QuizFixture fixture = createPublishedQuizFixture("pass-review", 70, 3);
		User student = saveStudent("student-pass-review@example.com");
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());

		var response = studentQuizService.submitAttempt(
			fixture.courseId(),
			fixture.lessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(fixture.firstQuestionId(), fixture.firstCorrectOptionId()),
				new AttemptAnswerRequest(fixture.secondQuestionId(), fixture.secondCorrectOptionId())
			))
		);

		assertThat(response.passed()).isTrue();
		assertThat(response.reviewAvailable()).isTrue();
		assertThat(response.remainingAttempts()).isEqualTo(2);
		assertThat(response.answers()).hasSize(2);
		assertThat(response.answers()).allMatch(answer -> Boolean.TRUE.equals(answer.correct()));
	}

	@Test
	void studentReceivesDetailedReviewOnLastAllowedAttemptEvenWhenFailing() {
		QuizFixture fixture = createPublishedQuizFixture("last-attempt-review", 100, 1);
		User student = saveStudent("student-last-attempt-review@example.com");
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());

		var response = studentQuizService.submitAttempt(
			fixture.courseId(),
			fixture.lessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(fixture.firstQuestionId(), fixture.firstCorrectOptionId()),
				new AttemptAnswerRequest(fixture.secondQuestionId(), fixture.secondWrongOptionId())
			))
		);

		assertThat(response.passed()).isFalse();
		assertThat(response.reviewAvailable()).isTrue();
		assertThat(response.remainingAttempts()).isZero();
		assertThat(response.answers()).hasSize(2);
	}

	@Test
	void studentCannotSubmitAttemptWithoutEnrollment() {
		QuizFixture fixture = createPublishedQuizFixture("no-enrollment-quiz", 70, 3);
		User student = saveStudent("student-no-enrollment-quiz@example.com");
		authenticate(student);

		assertThatThrownBy(() -> studentQuizService.submitAttempt(
			fixture.courseId(),
			fixture.lessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(fixture.firstQuestionId(), fixture.firstCorrectOptionId()),
				new AttemptAnswerRequest(fixture.secondQuestionId(), fixture.secondCorrectOptionId())
			))
		)).isInstanceOf(ForbiddenException.class)
			.hasMessage("Student is not enrolled in this course.");
	}

	@Test
	void studentCannotExceedQuizAttemptLimit() {
		QuizFixture fixture = createPublishedQuizFixture("max-attempts-quiz", 50, 1);
		User student = saveStudent("student-max-attempts@example.com");
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());

		studentQuizService.submitAttempt(
			fixture.courseId(),
			fixture.lessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(fixture.firstQuestionId(), fixture.firstCorrectOptionId()),
				new AttemptAnswerRequest(fixture.secondQuestionId(), fixture.secondCorrectOptionId())
			))
		);

		assertThatThrownBy(() -> studentQuizService.submitAttempt(
			fixture.courseId(),
			fixture.lessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(fixture.firstQuestionId(), fixture.firstCorrectOptionId()),
				new AttemptAnswerRequest(fixture.secondQuestionId(), fixture.secondCorrectOptionId())
			))
		)).isInstanceOf(ConflictException.class)
			.hasMessage("Student has reached the maximum number of attempts for this quiz.");
	}

	@Test
	void studentMustAnswerEachQuestionExactlyOnce() {
		QuizFixture fixture = createPublishedQuizFixture("duplicate-question-quiz", 70, 3);
		User student = saveStudent("student-duplicate-question@example.com");
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());

		assertThatThrownBy(() -> studentQuizService.submitAttempt(
			fixture.courseId(),
			fixture.lessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(fixture.firstQuestionId(), fixture.firstCorrectOptionId()),
				new AttemptAnswerRequest(fixture.firstQuestionId(), fixture.firstWrongOptionId())
			))
		)).isInstanceOf(ConflictException.class)
			.hasMessage("Student must answer each quiz question only once.");
	}

	private QuizFixture createPublishedQuizFixture(String prefix, int passingScore, int maxAttempts) {
		User teacher = saveTeacher(prefix + "-teacher@example.com");
		authenticate(teacher);
		var course = courseService.create(courseMapper.toEntity(
			new CourseCreateRequest("Course " + prefix, "Description", "BEGINNER")
		));
		var module = moduleService.create(course.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module", "Description", 1)
		));
		var lesson = lessonService.create(course.id(), module.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson", "Description", "content", "https://video", 1)
		));
		var quiz = quizService.create(course.id(), module.id(), lesson.id(), quizMapper.toEntity(
			new QuizCreateRequest("Quiz", passingScore, maxAttempts, 2)
		));
		var firstQuestion = questionService.create(course.id(), module.id(), lesson.id(), questionMapper.toEntity(
			new QuestionCreateRequest("Question 1", 1)
		));
		var secondQuestion = questionService.create(course.id(), module.id(), lesson.id(), questionMapper.toEntity(
			new QuestionCreateRequest("Question 2", 2)
		));
		var firstCorrect = optionService.create(course.id(), module.id(), lesson.id(), firstQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Option 1A", true, 1)
		));
		var firstWrong = optionService.create(course.id(), module.id(), lesson.id(), firstQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Option 1B", false, 2)
		));
		var secondCorrect = optionService.create(course.id(), module.id(), lesson.id(), secondQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Option 2A", true, 1)
		));
		var secondWrong = optionService.create(course.id(), module.id(), lesson.id(), secondQuestion.id(), optionMapper.toEntity(
			new OptionCreateRequest("Option 2B", false, 2)
		));
		courseService.publish(course.id());

		return new QuizFixture(
			course.id(),
			lesson.id(),
			quiz.id(),
			firstQuestion.id(),
			secondQuestion.id(),
			firstCorrect.id(),
			firstWrong.id(),
			secondCorrect.id(),
			secondWrong.id()
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

	private User saveTeacher(String email) {
		return userRepository.save(User.createTeacher(email, "password-hash", "Teacher"));
	}

	private User saveStudent(String email) {
		return userRepository.save(User.createStudent(email, "password-hash", "Student"));
	}

	private record QuizFixture(
		UUID courseId,
		UUID lessonId,
		UUID quizId,
		UUID firstQuestionId,
		UUID secondQuestionId,
		UUID firstCorrectOptionId,
		UUID firstWrongOptionId,
		UUID secondCorrectOptionId,
		UUID secondWrongOptionId
	) {
	}
}

