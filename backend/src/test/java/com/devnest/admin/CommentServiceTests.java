package com.devnest.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.admin.service.lessoncomment.LessonCommentService;
import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.common.exception.ConflictException;
import com.devnest.course.dto.module.ModuleCreateRequest;
import com.devnest.course.entity.comment.CommentStatus;
import com.devnest.course.entity.comment.Comment;
import com.devnest.course.dto.course.CourseCreateRequest;
import com.devnest.course.dto.comment.CommentCreateRequest;
import com.devnest.course.dto.lesson.LessonCreateRequest;
import com.devnest.course.mapper.course.CourseMapper;
import com.devnest.course.mapper.module.ModuleMapper;
import com.devnest.course.mapper.lesson.LessonMapper;
import com.devnest.course.repository.comment.CommentRepository;
import com.devnest.course.service.course.CourseEnrollmentService;
import com.devnest.course.service.module.ModuleService;
import com.devnest.course.service.course.CourseService;
import com.devnest.course.service.comment.CommentService;
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
class CommentServiceTests {

	@Autowired
	private LessonCommentService lessonCommentService;

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
	private CommentRepository commentRepository;

	@Autowired
	private CourseMapper courseMapper;

	@Autowired
	private ModuleMapper moduleMapper;

	@Autowired
	private LessonMapper lessonMapper;

	@Autowired
	private UserRepository userRepository;

	@Test
	void adminCanListRetainedCommentsWithCourseAndLessonContext() {
		Comment retainedByFilter = createRetainedComment("retained-filter", CommentStatus.HIDDEN_BY_FILTER);
		Comment retainedByTeacher = createRetainedComment("retained-teacher", CommentStatus.REMOVED_BY_TEACHER);
		createVisibleComment("visible-comment");
		User admin = saveAdmin("admin-retained-list@example.com");
		authenticate(admin);

		var response = lessonCommentService.findRetainedComments();

		assertThat(response).hasSize(2);
		assertThat(response).extracting(item -> item.id())
			.contains(retainedByFilter.getId(), retainedByTeacher.getId());
		assertThat(response).allSatisfy(item -> {
			assertThat(item.courseId()).isNotNull();
			assertThat(item.courseTitle()).isNotBlank();
			assertThat(item.lessonId()).isNotNull();
			assertThat(item.lessonTitle()).isNotBlank();
		});
	}

	@Test
	void adminCanDeleteRetainedCommentById() {
		Comment retained = createRetainedComment("delete-retained", CommentStatus.HIDDEN_BY_FILTER);
		User admin = saveAdmin("admin-delete-retained@example.com");
		authenticate(admin);

		lessonCommentService.deleteRetainedComment(retained.getId());

		assertThat(commentRepository.findById(retained.getId())).isEmpty();
	}

	@Test
	void adminCannotDeleteCommentThatIsNotRetained() {
		Comment visible = createVisibleComment("delete-visible");
		User admin = saveAdmin("admin-delete-visible@example.com");
		authenticate(admin);

		assertThatThrownBy(() -> lessonCommentService.deleteRetainedComment(visible.getId()))
			.isInstanceOf(ConflictException.class)
			.hasMessage("Only retained comments can be deleted from the admin queue.");
	}

	private Comment createRetainedComment(String prefix, CommentStatus status) {
		Comment comment = createVisibleComment(prefix);
		comment.setStatus(status);
		comment.setModerationReason("Retained for admin review");
		comment.setRemovedAt(java.time.OffsetDateTime.now());
		return commentRepository.save(comment);
	}

	private Comment createVisibleComment(String prefix) {
		LessonFixture fixture = createPublishedLessonFixture(prefix);
		User student = saveStudent(prefix + "-student@example.com");
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
		User teacher = saveTeacher(prefix + "-teacher@example.com");
		authenticate(teacher);
		var course = courseService.create(courseMapper.toEntity(
			new CourseCreateRequest("Course " + prefix, "Description", "BEGINNER")
		));
		var module = moduleService.create(course.id(), moduleMapper.toEntity(
			new ModuleCreateRequest("Module", "Description", 1)
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

	private record LessonFixture(UUID courseId, UUID lessonId) {
	}
}
