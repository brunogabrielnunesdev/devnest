package com.devnest.projects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.project.entity.project.ProjectVisibility;
import com.devnest.project.dto.project.CreateProjectRequest;
import com.devnest.project.dto.project.updateproject.ProjectUpdateCreateRequest;
import com.devnest.project.dto.project.updateproject.ProjectUpdateUpdateRequest;
import com.devnest.project.mapper.ProjectUpdateMapper;
import com.devnest.project.service.project.ProjectService;
import com.devnest.project.service.project.ProjectUpdateService;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.identity.entity.User;
import com.devnest.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class ProjectUpdateServiceTests {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectUpdateService projectUpdateService;

	@Autowired
	private ProjectUpdateMapper projectUpdateMapper;

	@Autowired
	private UserRepository userRepository;

	@Test
	void ownerCanCreateUpdateInsideOwnedProject() {
		User owner = saveStudent("project-update-create@example.com");
		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Project", "Description", ProjectVisibility.PUBLIC));

		var response = projectUpdateService.create(
			project.id(),
			projectUpdateMapper.toEntity(new ProjectUpdateCreateRequest("Kickoff", "Started implementation"))
		);

		assertThat(response.id()).isNotNull();
		assertThat(response.projectId()).isEqualTo(project.id());
		assertThat(response.title()).isEqualTo("Kickoff");
	}

	@Test
	void ownerCanUpdateOwnProjectUpdate() {
		User owner = saveStudent("project-update-edit@example.com");
		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Project", "Description", ProjectVisibility.PUBLIC));
		var update = projectUpdateService.create(
			project.id(),
			projectUpdateMapper.toEntity(new ProjectUpdateCreateRequest("Kickoff", "Started"))
		);

		var response = projectUpdateService.update(
			project.id(),
			update.id(),
			projectUpdateMapper.toEntity(new ProjectUpdateUpdateRequest("Milestone 1", "Finished API"))
		);

		assertThat(response.title()).isEqualTo("Milestone 1");
		assertThat(response.content()).isEqualTo("Finished API");
	}

	@Test
	void userCannotCreateUpdateInAnotherUsersProject() {
		User owner = saveStudent("project-owner-updates@example.com");
		User otherUser = saveStudent("project-other-updates@example.com");
		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Project", "Description", ProjectVisibility.PRIVATE));

		authenticate(otherUser);

		assertThatThrownBy(() -> projectUpdateService.create(
			project.id(),
			projectUpdateMapper.toEntity(new ProjectUpdateCreateRequest("Intrusion", "Should fail"))
		)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void ownerCanListOwnProjectUpdates() {
		User owner = saveStudent("project-update-list@example.com");
		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Project", "Description", ProjectVisibility.PUBLIC));
		projectUpdateService.create(project.id(), projectUpdateMapper.toEntity(new ProjectUpdateCreateRequest("One", "First")));
		projectUpdateService.create(project.id(), projectUpdateMapper.toEntity(new ProjectUpdateCreateRequest("Two", "Second")));

		var response = projectUpdateService.findAll(project.id());

		assertThat(response).hasSize(2);
		assertThat(response).allMatch(update -> update.projectId().equals(project.id()));
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

