package com.devnest.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.common.exception.ConflictException;
import com.devnest.common.exception.ForbiddenException;
import com.devnest.course.dto.module.ModuleCreateRequest;
import com.devnest.course.mapper.module.ModuleMapper;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import com.devnest.identity.repository.UserRepository;
import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.course.dto.course.CourseCreateRequest;
import com.devnest.course.dto.lesson.LessonCreateRequest;
import com.devnest.course.service.course.CourseEnrollmentService;
import com.devnest.course.service.module.ModuleService;
import com.devnest.course.service.course.CourseService;
import com.devnest.course.service.comment.CommentService;
import com.devnest.course.service.lesson.LessonProgressService;
import com.devnest.course.service.lesson.LessonService;
import com.devnest.course.mapper.course.CourseMapper;
import com.devnest.course.mapper.lesson.LessonMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class CommentFlowTests {

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
	private CommentService commentService;

	@Autowired
	private CourseMapper courseMapper;

	@Autowired
	private ModuleMapper moduleMapper;

	@Autowired
	private LessonMapper lessonMapper;

	@Autowired
	private UserRepository userRepository;

	@Test
	void studentCannotCommentBeforeCompletingLesson() {
		LessonFixture fixture = createPublishedLessonFixture("comment-before-complete");
		User student = saveStudent("student-before-complete@example.com");
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());

		assertThatThrownBy(() -> commentService.create(
			fixture.courseId(),
			fixture.lessonId(),
			"Great lesson",
			9
		)).isInstanceOf(ForbiddenException.class)
			.hasMessage("Student must complete the lesson before commenting.");
	}

	@Test
	void completedStudentCanCreateAndListVisibleComments() {
		LessonFixture fixture = createPublishedLessonFixture("comment-visible");
		User student = saveStudent("student-visible@example.com");
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());
		lessonProgressService.completeLesson(fixture.courseId(), fixture.lessonId());

		var created = commentService.create(fixture.courseId(), fixture.lessonId(), "  Great lesson  ", 10);
		var comments = commentService.findVisibleComments(fixture.courseId(), fixture.lessonId());

		assertThat(created.content()).isEqualTo("Great lesson");
		assertThat(created.status().name()).isEqualTo("VISIBLE");
		assertThat(comments).hasSize(1);
		assertThat(comments.get(0).id()).isEqualTo(created.id());
	}

	@Test
	void teacherCanModerateOwnedLessonCommentAndHideItFromVisibleList() {
		LessonFixture fixture = createPublishedLessonFixture("comment-teacher-moderation");
		User student = saveStudent("student-teacher-moderation@example.com");
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());
		lessonProgressService.completeLesson(fixture.courseId(), fixture.lessonId());
		var created = commentService.create(fixture.courseId(), fixture.lessonId(), "Needs review", 4);

		authenticate(fixture.teacher());
		var moderated = commentService.moderateByTeacher(
			fixture.courseId(),
			fixture.moduleId(),
			fixture.lessonId(),
			created.id(),
			"Off-topic"
		);

		authenticate(student);
		var visibleComments = commentService.findVisibleComments(fixture.courseId(), fixture.lessonId());

		assertThat(moderated.status().name()).isEqualTo("REMOVED_BY_TEACHER");
		assertThat(moderated.moderationReason()).isEqualTo("Off-topic");
		assertThat(visibleComments).isEmpty();
	}

	@Test
	void adminCanModerateAnyComment() {
		LessonFixture fixture = createPublishedLessonFixture("comment-admin-moderation");
		User student = saveStudent("student-admin-moderation@example.com");
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());
		lessonProgressService.completeLesson(fixture.courseId(), fixture.lessonId());
		var created = commentService.create(fixture.courseId(), fixture.lessonId(), "Spam", 2);

		User admin = saveAdmin("admin-comments@example.com");
		authenticate(admin);
		var moderated = commentService.moderateByAdmin(created.id(), "Policy violation");

		assertThat(moderated.status().name()).isEqualTo("REMOVED_BY_ADMIN");
		assertThat(moderated.moderationReason()).isEqualTo("Policy violation");
		assertThat(moderated.removedBy()).isEqualTo(admin.getId());
	}

	@Test
	void moderatedCommentCannotBeModeratedAgain() {
		LessonFixture fixture = createPublishedLessonFixture("comment-double-moderation");
		User student = saveStudent("student-double-moderation@example.com");
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());
		lessonProgressService.completeLesson(fixture.courseId(), fixture.lessonId());
		var created = commentService.create(fixture.courseId(), fixture.lessonId(), "Duplicate", 3);

		authenticate(fixture.teacher());
		commentService.moderateByTeacher(
			fixture.courseId(),
			fixture.moduleId(),
			fixture.lessonId(),
			created.id(),
			"Already handled"
		);

		User admin = saveAdmin("admin-double-moderation@example.com");
		authenticate(admin);

		assertThatThrownBy(() -> commentService.moderateByAdmin(created.id(), "Second pass"))
			.isInstanceOf(ConflictException.class)
			.hasMessage("Comment has already been moderated.");
	}

	private LessonFixture createPublishedLessonFixture(String prefix) {
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

		return new LessonFixture(course.id(), module.id(), lesson.id(), teacher);
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

	private User saveAdmin(String email) {
		User admin = User.createStudent(email, "password-hash", "Admin");
		admin.setRole(UserRole.ADMIN);
		return userRepository.save(admin);
	}

	private record LessonFixture(
		UUID courseId,
		UUID moduleId,
		UUID lessonId,
		User teacher
	) {
	}
}

