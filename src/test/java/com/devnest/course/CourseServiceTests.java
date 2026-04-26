package com.devnest.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.security.AuthenticatedUser;
import com.devnest.course.domain.CourseStatus;
import com.devnest.course.dto.CourseCreateRequest;
import com.devnest.course.dto.CourseUpdateRequest;
import com.devnest.course.mapper.CourseMapper;
import com.devnest.course.repository.CourseRepository;
import com.devnest.shared.exception.ForbiddenException;
import com.devnest.shared.exception.ResourceNotFoundException;
import com.devnest.user.domain.User;
import com.devnest.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class CourseServiceTests {

	@Autowired
	private CourseService courseService;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private CourseMapper courseMapper;

	@Autowired
	private UserRepository userRepository;

	@Test
	void teacherCreatesDraftCourse() {
		User teacher = saveTeacher("teacher-course-create@example.com");
		authenticate(teacher);

		var response = courseService.create(createCourse("Spring Boot", "Backend course", "BEGINNER"));

		assertThat(response.id()).isNotNull();
		assertThat(response.teacherId()).isEqualTo(teacher.getId());
		assertThat(response.title()).isEqualTo("Spring Boot");
		assertThat(response.status()).isEqualTo(CourseStatus.DRAFT);
		assertThat(courseRepository.findById(response.id())).isPresent();
	}

	@Test
	void studentCannotCreateCourse() {
		User student = saveStudent("student-course-create@example.com");
		authenticate(student);

		assertThatThrownBy(() -> courseService.create(
			createCourse("React", "Frontend course", "BEGINNER")
		)).isInstanceOf(ForbiddenException.class);
	}

	@Test
	void teacherCanUpdateOwnCourse() {
		User teacher = saveTeacher("teacher-course-update@example.com");
		authenticate(teacher);
		var course = courseService.create(
			createCourse("Old title", "Old description", "BEGINNER")
		);

		var response = courseService.update(
			course.id(),
			updateCourse("New title", "New description", "INTERMEDIATE")
		);

		assertThat(response.title()).isEqualTo("New title");
		assertThat(response.description()).isEqualTo("New description");
		assertThat(response.level()).isEqualTo("INTERMEDIATE");
	}

	@Test
	void teacherCannotUpdateAnotherTeachersCourse() {
		User owner = saveTeacher("teacher-course-owner@example.com");
		User otherTeacher = saveTeacher("teacher-course-other@example.com");
		authenticate(owner);
		var course = courseService.create(
			createCourse("Owner course", "Description", "BEGINNER")
		);

		authenticate(otherTeacher);
		assertThatThrownBy(() -> courseService.update(
			course.id(),
			updateCourse("Changed", "Changed", "ADVANCED")
		)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void teacherArchivesOwnCourse() {
		User teacher = saveTeacher("teacher-course-archive@example.com");
		authenticate(teacher);
		var course = courseService.create(
			createCourse("Course to archive", "Description", "BEGINNER")
		);

		courseService.delete(course.id());
		var archivedCourse = courseRepository.findById(course.id()).orElseThrow();

		assertThat(archivedCourse.getStatus()).isEqualTo(CourseStatus.ARCHIVED);
	}

	private com.devnest.course.domain.Course createCourse(String title, String description, String level) {
		return courseMapper.toEntity(new CourseCreateRequest(title, description, level));
	}

	private com.devnest.course.domain.Course updateCourse(String title, String description, String level) {
		return courseMapper.toEntity(new CourseUpdateRequest(title, description, level));
	}

	private void authenticate(User user) {
		AuthenticatedUser authenticatedUser = new AuthenticatedUser(user);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			authenticatedUser,
			null,
			authenticatedUser.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private User saveTeacher(String email) {
		return userRepository.save(User.createTeacher(email, "password-hash", "Teacher"));
	}

	private User saveStudent(String email) {
		return userRepository.save(User.createStudent(email, "password-hash", "Student"));
	}
}
