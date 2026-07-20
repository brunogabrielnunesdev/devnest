package com.devnest.projects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devnest.auth.security.useridentity.CustomAuthentication;
import com.devnest.common.exception.ForbiddenException;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.identity.entity.User;
import com.devnest.identity.repository.UserRepository;
import com.devnest.project.dto.project.CreateProjectRequest;
import com.devnest.project.dto.members.MemberCreateRequest;
import com.devnest.project.dto.note.NoteCreateRequest;
import com.devnest.project.dto.task.TaskCreateRequest;
import com.devnest.project.entity.member.MemberRole;
import com.devnest.project.entity.task.TaskPriority;
import com.devnest.project.entity.task.TaskStatus;
import com.devnest.project.entity.project.ProjectVisibility;
import com.devnest.project.service.member.MemberService;
import com.devnest.project.service.note.NoteService;
import com.devnest.project.service.project.ProjectService;
import com.devnest.project.service.task.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
class ProjectCollaborationServiceTests {

	@Autowired
	private MemberService memberService;

	@Autowired
	private NoteService noteService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void adminMemberCanManageTasksAndNotes() {
		User owner = saveStudent("project-admin-owner@example.com");
		User admin = saveStudent("project-admin@example.com");

		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Shared project", "Description", ProjectVisibility.PRIVATE));
		memberService.create(project.id(), new MemberCreateRequest(admin.getId(), MemberRole.ADMIN));

		authenticate(admin);
		var task = taskService.create(project.id(), new TaskCreateRequest(
			"Admin task",
			"Task description",
			TaskStatus.TODO,
			TaskPriority.MEDIUM,
			null,
			null
		));
		var note = noteService.create(project.id(), new NoteCreateRequest("Admin note"));

		assertThat(task.projectId()).isEqualTo(project.id());
		assertThat(note.projectId()).isEqualTo(project.id());
	}

	@Test
	void viewerMemberCanViewButCannotManageContent() {
		User owner = saveStudent("project-viewer-owner@example.com");
		User viewer = saveStudent("project-viewer@example.com");

		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Private project", "Description", ProjectVisibility.PRIVATE));
		memberService.create(project.id(), new MemberCreateRequest(viewer.getId(), MemberRole.VIEWER));

		authenticate(viewer);
		assertThat(projectService.findById(project.id()).id()).isEqualTo(project.id());
		assertThatThrownBy(() -> taskService.create(project.id(), new TaskCreateRequest(
			"Blocked task",
			"Nope",
			TaskStatus.TODO,
			TaskPriority.LOW,
			null,
			null
		))).isInstanceOf(ForbiddenException.class);
	}

	@Test
	void nonMemberCannotViewPrivateProject() {
		User owner = saveStudent("project-private-owner@example.com");
		User outsider = saveStudent("project-outsider@example.com");

		authenticate(owner);
		var project = projectService.create(new CreateProjectRequest("Secret", "Description", ProjectVisibility.PRIVATE));

		authenticate(outsider);
		assertThatThrownBy(() -> projectService.findById(project.id()))
			.isInstanceOf(ResourceNotFoundException.class);
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
