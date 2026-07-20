package com.devnest.projects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.project.dto.project.CreateProjectRequest;
import com.devnest.project.dto.task.TaskCreateRequest;
import com.devnest.project.entity.project.ProjectStatus;
import com.devnest.project.entity.task.TaskPriority;
import com.devnest.project.entity.task.TaskStatus;
import com.devnest.project.entity.project.ProjectVisibility;
import com.devnest.project.dto.project.updateproject.ProjectUpdateRequest;
import com.devnest.project.service.task.TaskService;
import com.devnest.project.repository.project.ProjectRepository;
import com.devnest.project.service.project.ProjectService;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.identity.entity.User;
import com.devnest.identity.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class ProjectServiceTests {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private TaskService taskService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void authenticatedUserCreatesPlanningProject() {
		User owner = saveStudent("project-owner@example.com");
		authenticate(owner);

		var response = projectService.create(new CreateProjectRequest("DevNest API", "Backend roadmap", ProjectVisibility.PUBLIC));

		assertThat(response.id()).isNotNull();
		assertThat(response.owner().id()).isEqualTo(owner.getId());
		assertThat(response.status()).isEqualTo(ProjectStatus.PLANNING);
		assertThat(response.visibility()).isEqualTo(ProjectVisibility.PUBLIC);
		assertThat(response.progress()).isZero();
		assertThat(response.totalTasks()).isZero();
		assertThat(response.completedTasks()).isZero();
		assertThat(projectRepository.findById(response.id())).isPresent();
	}

	@Test
	void ownerCanUpdateOwnProject() {
		User owner = saveStudent("project-update-owner@example.com");
		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Old title", "Old description", ProjectVisibility.PRIVATE));

		var response = projectService.update(
			project.id(),
			new ProjectUpdateRequest("New title", "New description", ProjectStatus.IN_PROGRESS, ProjectVisibility.PUBLIC)
		);

		assertThat(response.title()).isEqualTo("New title");
		assertThat(response.description()).isEqualTo("New description");
		assertThat(response.status()).isEqualTo(ProjectStatus.IN_PROGRESS);
		assertThat(response.visibility()).isEqualTo(ProjectVisibility.PUBLIC);
	}

	@Test
	void userCannotUpdateAnotherUsersProject() {
		User owner = saveStudent("project-owner-locked@example.com");
		User otherUser = saveStudent("project-other-user@example.com");
		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Private project", "Description", ProjectVisibility.PRIVATE));

		authenticate(otherUser);

		assertThatThrownBy(() -> projectService.update(
			project.id(),
			new ProjectUpdateRequest("Changed", "Changed", ProjectStatus.PAUSED, ProjectVisibility.PRIVATE)
		)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void ownerDeletesProjectAndRelatedUpdates() {
		User owner = saveStudent("project-delete-owner@example.com");
		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Delete me", "Description", ProjectVisibility.PUBLIC));

		projectService.delete(project.id());

		assertThat(projectRepository.findById(project.id())).isEmpty();
	}

	@Test
	void progressIsCalculatedFromCompletedTasks() {
		User owner = saveStudent("project-progress-owner@example.com");
		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Progress", "Tracking", ProjectVisibility.PUBLIC));

		taskService.create(project.id(), new TaskCreateRequest(
			"Task 1",
			"Done task",
			TaskStatus.DONE,
			TaskPriority.HIGH,
			null,
			LocalDate.now().plusDays(3)
		));
		taskService.create(project.id(), new TaskCreateRequest(
			"Task 2",
			"In progress task",
			TaskStatus.IN_PROGRESS,
			TaskPriority.MEDIUM,
			null,
			null
		));

		var response = projectService.findById(project.id());

		assertThat(response.totalTasks()).isEqualTo(2);
		assertThat(response.completedTasks()).isEqualTo(1);
		assertThat(response.progress()).isEqualTo(50.0);
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

	private User saveStudent(String email) {
		return userRepository.save(User.createStudent(email, "password-hash", "Student"));
	}
}

