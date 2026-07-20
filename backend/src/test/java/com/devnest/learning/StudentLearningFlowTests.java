package com.devnest.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.common.exception.ConflictException;
import com.devnest.common.exception.ForbiddenException;
import com.devnest.course.dto.module.ModuleCreateRequest;
import com.devnest.course.dto.question.QuestionCreateRequest;
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
import com.devnest.course.dto.student.learning.StudentCourseLearningContentResponse;
import com.devnest.course.dto.lesson.LessonCreateRequest;
import com.devnest.course.dto.quiz.attempt.AttemptAnswerRequest;
import com.devnest.course.dto.quiz.attempt.AttemptSubmitRequest;
import com.devnest.course.dto.quiz.QuizCreateRequest;
import com.devnest.course.dto.option.OptionCreateRequest;
import com.devnest.course.entity.course.CourseStatus;
import com.devnest.course.entity.course.EnrollmentStatus;
import com.devnest.course.service.course.CourseEnrollmentService;
import com.devnest.course.service.module.ModuleService;
import com.devnest.course.service.course.CourseService;
import com.devnest.course.service.lesson.LessonProgressService;
import com.devnest.course.service.lesson.LessonService;
import com.devnest.course.service.student.StudentLearningContentService;
import com.devnest.course.service.option.OptionService;
import com.devnest.course.service.question.QuestionService;
import com.devnest.course.service.quiz.QuizService;
import com.devnest.course.service.student.StudentQuizService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class StudentLearningFlowTests {

	@Autowired
	private CourseService courseService;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private LessonService lessonService;

	@Autowired
	private CourseEnrollmentService courseEnrollmentService;

	@Autowired
	private LessonProgressService lessonProgressService;

	@Autowired
	private CourseMapper courseMapper;

	@Autowired
	private ModuleMapper moduleMapper;

	@Autowired
	private LessonMapper lessonMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private QuizService quizService;

	@Autowired
	private QuestionService questionService;

	@Autowired
	private OptionService optionService;

	@Autowired
	private StudentQuizService studentQuizService;

	@Autowired
	private StudentLearningContentService studentLearningContentService;

	@Autowired
	private QuizMapper quizMapper;

	@Autowired
	private QuestionMapper questionMapper;

	@Autowired
	private OptionMapper optionMapper;

	@Test
	void studentCannotEnrollInDraftCourse() {
		User teacher = saveTeacher("draft-teacher@example.com");
		User student = saveStudent("draft-student@example.com");
		authenticate(teacher);
		var course = courseService.create(courseMapper.toEntity(
			new CourseCreateRequest("Draft course", "Description", "BEGINNER")
		));

		authenticate(student);

		assertThatThrownBy(() -> courseEnrollmentService.enroll(course.id()))
			.isInstanceOf(com.devnest.common.exception.ResourceNotFoundException.class);
	}

	@Test
	void teacherCanPublishCourseWithAtLeastOneLesson() {
		User teacher = saveTeacher("publish-teacher@example.com");
		authenticate(teacher);
		var course = courseService.create(courseMapper.toEntity(
			new CourseCreateRequest("Course", "Description", "BEGINNER")
		));
		var module = moduleService.create(course.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module", "Description", 1)
		));
		lessonService.create(course.id(), module.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson", "Description", "content", "https://video", 1)
		));

		var response = courseService.publish(course.id());

		assertThat(response.status()).isEqualTo(CourseStatus.PUBLISHED);
	}

	@Test
	void studentCanEnrollOnlyOnceInPublishedCourse() {
		var published = createPublishedCourse("enroll-once");
		User student = saveStudent("student-enroll-once@example.com");
		authenticate(student);

		var enrollment = courseEnrollmentService.enroll(published.courseId());
		assertThat(enrollment.status()).isEqualTo(EnrollmentStatus.ACTIVE);

		assertThatThrownBy(() -> courseEnrollmentService.enroll(published.courseId()))
			.isInstanceOf(ConflictException.class)
			.hasMessage("Student is already enrolled in this course.");
	}

	@Test
	void studentCannotMarkLessonCompleteWithoutEnrollment() {
		var published = createPublishedCourse("progress-without-enrollment");
		User student = saveStudent("student-no-enrollment@example.com");
		authenticate(student);

		assertThatThrownBy(() -> lessonProgressService.completeLesson(published.courseId(), published.lessonId()))
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Student is not enrolled in this course.");
	}

	@Test
	void studentCanCompleteLessonWithoutQuiz() {
		var published = createPublishedCourse("progress-without-quiz");
		User student = saveStudent("student-progress-without-quiz@example.com");
		authenticate(student);

		courseEnrollmentService.enroll(published.courseId());
		var response = lessonProgressService.completeLesson(published.courseId(), published.lessonId());

		assertThat(response.completed()).isTrue();
	}

	@Test
	void studentCannotCompleteLessonWithQuizBeforePassing() {
		var published = createPublishedCourseWithQuiz("progress-with-quiz");
		User student = saveStudent("student-progress-with-quiz@example.com");
		authenticate(student);

		courseEnrollmentService.enroll(published.courseId());

		assertThatThrownBy(() -> lessonProgressService.completeLesson(published.courseId(), published.lessonId()))
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Student must pass the lesson quiz before completing this lesson.");
	}

	@Test
	void studentCanCompleteLessonAfterPassingQuiz() {
		var published = createPublishedCourseWithQuiz("progress-after-pass");
		User student = saveStudent("student-progress-after-pass@example.com");
		authenticate(student);

		courseEnrollmentService.enroll(published.courseId());
		studentQuizService.submitAttempt(
			published.courseId(),
			published.lessonId(),
			new AttemptSubmitRequest(List.of(
				new AttemptAnswerRequest(published.questionId(), published.correctOptionId())
			))
		);

		var response = lessonProgressService.completeLesson(published.courseId(), published.lessonId());

		assertThat(response.completed()).isTrue();
	}

	@Test
	void completingAllLessonsMarksEnrollmentAsCompleted() {
		var published = createPublishedCourseWithTwoLessons("complete-course");
		User student = saveStudent("student-complete@example.com");
		authenticate(student);

		courseEnrollmentService.enroll(published.courseId());
		lessonProgressService.completeLesson(published.courseId(), published.firstLessonId());
		var intermediate = lessonProgressService.getCourseProgress(published.courseId());
		assertThat(intermediate.enrollmentStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
		assertThat(intermediate.completedLessons()).isEqualTo(1);

		lessonProgressService.completeLesson(published.courseId(), published.secondLessonId());
		var completed = lessonProgressService.getCourseProgress(published.courseId());

		assertThat(completed.totalLessons()).isEqualTo(2);
		assertThat(completed.completedLessons()).isEqualTo(2);
		assertThat(completed.enrollmentStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
	}

	@Test
	void studentCanSeeLearningContentForEnrolledPublishedCourse() {
		var published = createPublishedCourseWithTwoLessons("learning-content");
		User student = saveStudent("student-learning-content@example.com");
		authenticate(student);

		courseEnrollmentService.enroll(published.courseId());
		lessonProgressService.completeLesson(published.courseId(), published.firstLessonId());

		StudentCourseLearningContentResponse response = studentLearningContentService.getLearningContent(published.courseId());

		assertThat(response.courseId()).isEqualTo(published.courseId());
		assertThat(response.status()).isEqualTo(CourseStatus.PUBLISHED);
		assertThat(response.modules()).hasSize(1);
		assertThat(response.modules().get(0).lessons()).hasSize(2);
		assertThat(response.modules().get(0).lessons().get(0).lessonId()).isEqualTo(published.firstLessonId());
		assertThat(response.modules().get(0).lessons().get(0).completed()).isTrue();
		assertThat(response.modules().get(0).lessons().get(1).lessonId()).isEqualTo(published.secondLessonId());
		assertThat(response.modules().get(0).lessons().get(1).completed()).isFalse();
	}

	@Test
	void studentCannotSeeLearningContentWithoutEnrollment() {
		var published = createPublishedCourse("learning-content-no-enrollment");
		User student = saveStudent("student-learning-content-no-enrollment@example.com");
		authenticate(student);

		assertThatThrownBy(() -> studentLearningContentService.getLearningContent(published.courseId()))
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Student is not enrolled in this course.");
	}

	private PublishedCourse createPublishedCourse(String prefix) {
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
		courseService.publish(course.id());
		return new PublishedCourse(course.id(), lesson.id());
	}

	private PublishedCourseWithQuiz createPublishedCourseWithQuiz(String prefix) {
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
			new QuizCreateRequest("Quiz", 100, 3, 1)
		));
		var question = questionService.create(course.id(), module.id(), lesson.id(), questionMapper.toEntity(
			new QuestionCreateRequest("Question 1", 1)
		));
		var correctOption = optionService.create(course.id(), module.id(), lesson.id(), question.id(), optionMapper.toEntity(
			new OptionCreateRequest("Option 1A", true, 1)
		));
		optionService.create(course.id(), module.id(), lesson.id(), question.id(), optionMapper.toEntity(
			new OptionCreateRequest("Option 1B", false, 2)
		));
		courseService.publish(course.id());
		return new PublishedCourseWithQuiz(course.id(), lesson.id(), quiz.id(), question.id(), correctOption.id());
	}

	private PublishedCourseWithTwoLessons createPublishedCourseWithTwoLessons(String prefix) {
		User teacher = saveTeacher(prefix + "-teacher@example.com");
		authenticate(teacher);
		var course = courseService.create(courseMapper.toEntity(
			new CourseCreateRequest("Course " + prefix, "Description", "BEGINNER")
		));
		var module = moduleService.create(course.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module", "Description", 1)
		));
		var firstLesson = lessonService.create(course.id(), module.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson 1", "Description", "content", "https://video", 1)
		));
		var secondLesson = lessonService.create(course.id(), module.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson 2", "Description", "content", "https://video", 2)
		));
		courseService.publish(course.id());
		return new PublishedCourseWithTwoLessons(course.id(), firstLesson.id(), secondLesson.id());
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

	private record PublishedCourse(java.util.UUID courseId, java.util.UUID lessonId) {
	}

	private record PublishedCourseWithQuiz(
		java.util.UUID courseId,
		java.util.UUID lessonId,
		java.util.UUID quizId,
		java.util.UUID questionId,
		java.util.UUID correctOptionId
	) {
	}

	private record PublishedCourseWithTwoLessons(java.util.UUID courseId, java.util.UUID firstLessonId, java.util.UUID secondLessonId) {
	}
}

