package com.devnest.projects;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.common.exception.ConflictException;
import com.devnest.identity.entity.User;
import com.devnest.identity.repository.UserRepository;
import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.project.dto.project.CreateProjectRequest;
import com.devnest.project.dto.project.updateproject.ProjectUpdateRequest;
import com.devnest.project.entity.project.ProjectStatus;
import com.devnest.project.entity.project.ProjectVisibility;
import com.devnest.project.service.project.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class ProjectBusinessRulesTests {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void projectCannotMoveDirectlyFromPlanningToCompleted() {
		User owner = saveStudent("project-transition-planning@example.com");
		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Project", "Description", ProjectVisibility.PUBLIC));

		assertThatThrownBy(() -> projectService.update(
			project.id(),
			new ProjectUpdateRequest(
				"Project",
				"Description",
				ProjectStatus.COMPLETED,
				ProjectVisibility.PUBLIC
			)
		)).isInstanceOf(ConflictException.class)
			.hasMessage("Project cannot move directly from planning to completed.");
	}

	@Test
	void completedProjectCannotChangeStatus() {
		User owner = saveStudent("project-transition-completed@example.com");
		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Project", "Description", ProjectVisibility.PUBLIC));

		projectService.update(project.id(),
			new ProjectUpdateRequest("Project", "Description", ProjectStatus.IN_PROGRESS, ProjectVisibility.PUBLIC)
		);
		projectService.update(project.id(),
			new ProjectUpdateRequest("Project", "Description", ProjectStatus.COMPLETED, ProjectVisibility.PUBLIC)
		);

		assertThatThrownBy(() -> projectService.update(
			project.id(),
			new ProjectUpdateRequest(
				"Project",
				"Description",
				ProjectStatus.PAUSED,
				ProjectVisibility.PUBLIC
			)
		)).isInstanceOf(ConflictException.class)
			.hasMessage("Completed projects cannot change status.");
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

