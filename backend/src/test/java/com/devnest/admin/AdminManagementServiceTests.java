package com.devnest.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.admin.service.comment.CommentService;
import com.devnest.admin.service.course.CourseService;
import com.devnest.admin.service.metrics.MetricsService;
import com.devnest.admin.service.user.UserService;
import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.common.exception.ConflictException;
import com.devnest.course.dto.course.CourseCreateRequest;
import com.devnest.course.dto.module.ModuleCreateRequest;
import com.devnest.course.dto.comment.CommentCreateRequest;
import com.devnest.course.dto.lesson.LessonCreateRequest;
import com.devnest.course.entity.comment.Comment;
import com.devnest.course.mapper.course.CourseMapper;
import com.devnest.course.mapper.module.ModuleMapper;
import com.devnest.course.mapper.lesson.LessonMapper;
import com.devnest.course.repository.course.CourseRepository;
import com.devnest.course.repository.comment.CommentRepository;
import com.devnest.course.service.course.CourseEnrollmentService;
import com.devnest.course.service.module.ModuleService;
import com.devnest.course.service.lesson.LessonProgressService;
import com.devnest.course.service.lesson.LessonService;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import com.devnest.identity.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class AdminManagementServiceTests {

	@Autowired
	private com.devnest.admin.service.course.CourseService adminCourseService;

	@Autowired
	private com.devnest.course.service.course.CourseService courseService;

	@Autowired
	private com.devnest.course.service.comment.CommentService commentService;

	@Autowired
	private CommentService adminCommentService;

	@Autowired
	private UserService userService;

	@Autowired
	private MetricsService metricsService;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private LessonService lessonService;

	@Autowired
	private CourseEnrollmentService courseEnrollmentService;

	@Autowired
	private LessonProgressService lessonProgressService;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CourseMapper courseMapper;

	@Autowired
	private ModuleMapper moduleMapper;

	@Autowired
	private LessonMapper lessonMapper;

	@Test
	void adminCanSearchArchiveRestoreAndDeleteCourses() {
		User admin = saveAdmin("admin-course-manage@example.com");
		LessonFixture javaFixture = createPublishedLessonFixture("java-admin");
		LessonFixture reactFixture = createPublishedLessonFixture("react-admin");
		authenticate(admin);

		var page = adminCourseService.findAll("java-admin", 0, 10);
		assertThat(page.content()).hasSize(1);
		assertThat(page.content().getFirst().title()).contains("java-admin");

		var archived = adminCourseService.archive(javaFixture.courseId());
		assertThat(archived.archived()).isTrue();
		assertThat(courseRepository.findById(javaFixture.courseId()).orElseThrow().isArchived()).isTrue();

		var restored = adminCourseService.restore(javaFixture.courseId());
		assertThat(restored.archived()).isFalse();

		courseService.delete(reactFixture.courseId());
		assertThat(courseRepository.findById(reactFixture.courseId())).isEmpty();
	}

	@Test
	void adminCanSearchHideAndRestoreVisibleComments() {
		User admin = saveAdmin("admin-comment-manage@example.com");
		Comment javaComment = createVisibleComment("java-comment");
		createVisibleComment("react-comment");
		authenticate(admin);

		var page = adminCommentService.findAll("java-comment", 0, 10);
		assertThat(page.content()).hasSize(1);
		assertThat(page.content().getFirst().content()).contains("java-comment");

		var hidden = adminCommentService.hide(javaComment.getId());
		assertThat(hidden.hidden()).isTrue();
		assertThat(commentRepository.findById(javaComment.getId()).orElseThrow().isHidden()).isTrue();

		var restored = adminCommentService.restore(javaComment.getId());
		assertThat(restored.hidden()).isFalse();
	}

	@Test
	void adminCanSearchUsersAndUpdateRoleButCannotDemoteSelf() {
		User admin = saveAdmin("admin-user-manage@example.com");
		User student = saveStudent("student-searchable@example.com", "Searchable Student");
		authenticate(admin);

		var page = userService.findAll("searchable", 0, 10);
		assertThat(page.content()).hasSize(1);
		assertThat(page.content().getFirst().email()).isEqualTo(student.getEmail());

		var updated = userService.updateRole(student.getId(), UserRole.TEACHER);
		assertThat(updated.role()).isEqualTo(UserRole.TEACHER);
		assertThat(userRepository.findById(student.getId()).orElseThrow().getRole()).isEqualTo(UserRole.TEACHER);

		assertThatThrownBy(() -> userService.updateRole(admin.getId(), UserRole.STUDENT))
			.isInstanceOf(ConflictException.class)
			.hasMessage("Admins cannot remove their own admin role.");
	}

	@Test
	void adminMetricsReturnPlatformTotals() {
		User admin = saveAdmin("admin-metrics@example.com");
		createPublishedLessonFixture("metrics-course");
		createVisibleComment("metrics-comment");
		authenticate(admin);

		var metrics = metricsService.getMetrics();

		assertThat(metrics.totalUsers()).isGreaterThanOrEqualTo(3);
		assertThat(metrics.totalCourses()).isGreaterThanOrEqualTo(2);
		assertThat(metrics.totalComments()).isGreaterThanOrEqualTo(1);
	}

	private Comment createVisibleComment(String prefix) {
		LessonFixture fixture = createPublishedLessonFixture(prefix);
		User student = saveStudent(prefix + "-student@example.com", "Student " + prefix);
		authenticate(student);
		courseEnrollmentService.enroll(fixture.courseId());
		lessonProgressService.completeLesson(fixture.courseId(), fixture.lessonId());
		var response = commentService.create(
			fixture.courseId(),
			fixture.lessonId(),
			new CommentCreateRequest("Comment " + prefix, 8).content(),
			8
		);
		return commentRepository.findById(response.id()).orElseThrow();
	}

	private LessonFixture createPublishedLessonFixture(String prefix) {
		User teacher = saveTeacher(prefix + "-teacher@example.com", "Teacher " + prefix);
		authenticate(teacher);
		var course = courseService.create(courseMapper.toEntity(
			new CourseCreateRequest("Course " + prefix, "Description", "BEGINNER")
		));
		var module = moduleService.create(course.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module " + prefix, "Description", 1)
		));
		var lesson = lessonService.create(course.id(), module.id(), lessonMapper.toEntity(
			new LessonCreateRequest("Lesson " + prefix, "Description", "content", "https://video", 1)
		));
		courseService.publish(course.id());
		return new LessonFixture(course.id(), lesson.id());
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

	private User saveAdmin(String email) {
		User admin = User.createStudent(email, "password-hash", "Admin");
		admin.setRole(UserRole.ADMIN);
		return userRepository.save(admin);
	}

	private record LessonFixture(UUID courseId, UUID lessonId) {
	}
}
