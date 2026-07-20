package com.devnest.project.service.project;

import com.devnest.auth.security.useridentity.CustomUserProvider;
import com.devnest.common.exception.ForbiddenException;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.identity.entity.User;
import com.devnest.project.entity.project.Project;
import com.devnest.project.entity.member.Member;
import com.devnest.project.entity.member.MemberRole;
import com.devnest.project.entity.project.ProjectUpdate;
import com.devnest.project.entity.project.ProjectVisibility;
import com.devnest.project.repository.project.ProjectRepository;
import com.devnest.project.repository.member.MemberRepository;
import com.devnest.project.repository.project.ProjectUpdateRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectAccessService {

	private final CustomUserProvider customUserProvider;
	private final MemberRepository memberRepository;
	private final ProjectRepository projectRepository;
	private final ProjectUpdateRepository projectUpdateRepository;

	public User getAuthenticatedUser()	 {
		return customUserProvider.getAuthenticatedUser();
	}

	public Project getOwnedProject(UUID projectId) {
		User user = getAuthenticatedUser();
		return projectRepository.findByIdAndOwnerId(projectId, user.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Project not found."));
	}

	public Project getProjectForView(UUID projectId) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found."));

		if (!canView(project, getAuthenticatedUser())) {
			throw new ResourceNotFoundException("Project not found.");
		}

		return project;
	}

	public Project getProjectForProjectManagement(UUID projectId) {
		Project project = getProjectForView(projectId);
		validateOwner(project, getAuthenticatedUser());
		return project;
	}

	public Project getProjectForContentManagement(UUID projectId) {
		Project project = getProjectForView(projectId);
		validateContentManager(project, getAuthenticatedUser());
		return project;
	}

	public ProjectUpdate getOwnedProjectUpdate(UUID projectId, UUID updateId) {
		Project project = getOwnedProject(projectId);
		return projectUpdateRepository.findByIdAndProjectId(updateId, project.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Project update not found."));
	}

	public ProjectUpdate getProjectUpdateForView(UUID projectId, UUID updateId) {
		Project project = getProjectForView(projectId);
		return projectUpdateRepository.findByIdAndProjectId(updateId, project.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Project update not found."));
	}

	public ProjectUpdate getProjectUpdateForManagement(UUID projectId, UUID updateId) {
		Project project = getProjectForContentManagement(projectId);
		return projectUpdateRepository.findByIdAndProjectId(updateId, project.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Project update not found."));
	}

	public MemberRole getRole(Project project, User user) {
		if (project.getOwner().getId().equals(user.getId())) {
			return MemberRole.OWNER;
		}

		return memberRepository.findByProjectIdAndUserId(project.getId(), user.getId())
			.map(Member::getRole)
			.orElse(null);
	}

	public boolean canView(Project project, User user) {
		if (project.getOwner().getId().equals(user.getId())) {
			return true;
		}

		if (project.getVisibility() == ProjectVisibility.PUBLIC) {
			return true;
		}

		return memberRepository.existsByProjectIdAndUserId(project.getId(), user.getId());
	}

	private void validateOwner(Project project, User user) {
		if (!project.getOwner().getId().equals(user.getId())) {
			throw new ForbiddenException("Only the project owner can manage project settings.");
		}
	}

	private void validateContentManager(Project project, User user) {
		if (project.getOwner().getId().equals(user.getId())) {
			return;
		}

		MemberRole role = getRole(project, user);
		if (role != MemberRole.ADMIN) {
			throw new ForbiddenException("Only the project owner or admins can manage this resource.");
		}
	}
}
