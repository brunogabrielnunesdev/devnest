package com.devnest.project.mapper;

import com.devnest.identity.entity.User;
import com.devnest.project.dto.project.CreateProjectRequest;
import com.devnest.project.dto.project.ProjectResponse;
import com.devnest.project.dto.project.updateproject.ProjectUpdateRequest;
import com.devnest.project.dto.members.ProjectUserSummaryResponse;
import com.devnest.project.entity.project.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

	public Project toEntity(CreateProjectRequest request) {
		Project project = new Project();
		project.setTitle(request.title());
		project.setDescription(request.description());
		project.setVisibility(request.visibility());
		return project;
	}

	public void updateEntity(Project project, ProjectUpdateRequest request) {
		project.setTitle(request.title());
		project.setDescription(request.description());
		project.setStatus(request.status());
		project.setVisibility(request.visibility());
	}

	public ProjectResponse toResponse(Project project, long totalTasks, long completedTasks) {
		double progress = totalTasks == 0
			? 0.0
			: (completedTasks * 100.0) / totalTasks;

		return new ProjectResponse(
			project.getId(),
			project.getTitle(),
			project.getDescription(),
			project.getStatus(),
			project.getVisibility(),
			toUserSummary(project.getOwner()),
			progress,
			totalTasks,
			completedTasks,
			project.getCreatedAt(),
			project.getUpdatedAt()
		);
	}

	public ProjectUserSummaryResponse toUserSummary(User user) {
		String displayName = user.getProfile() != null ? user.getProfile().getDisplayName() : null;
		return new ProjectUserSummaryResponse(user.getId(), user.getEmail(), displayName);
	}
}
