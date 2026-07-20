package com.devnest.project.service.project;

import com.devnest.project.entity.project.Project;
import com.devnest.project.entity.project.ProjectStatus;
import com.devnest.project.entity.project.ProjectVisibility;
import com.devnest.project.entity.activitylogs.ProjectActivityType;
import com.devnest.project.entity.member.Member;
import com.devnest.project.entity.member.MemberRole;
import com.devnest.project.entity.task.TaskStatus;
import com.devnest.project.dto.project.CreateProjectRequest;
import com.devnest.project.dto.project.ProjectResponse;
import com.devnest.project.dto.project.updateproject.ProjectUpdateRequest;
import com.devnest.common.exception.ConflictException;
import com.devnest.project.mapper.ProjectMapper;
import com.devnest.project.repository.member.MemberRepository;
import com.devnest.project.repository.project.ProjectRepository;
import com.devnest.project.repository.task.TaskRepository;
import com.devnest.project.repository.project.ProjectUpdateRepository;
import java.util.List;
import java.util.UUID;

import com.devnest.project.service.activitylogs.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

	private final ProjectAccessService accessService;
	private final ProjectMapper projectMapper;
	private final ActivityLogService activityLogService;
	private final MemberRepository memberRepository;
	private final ProjectRepository projectRepository;
	private final TaskRepository taskRepository;
	private final ProjectUpdateRepository projectUpdateRepository;

	@Transactional
	public ProjectResponse create(CreateProjectRequest request) {
		var user = accessService.getAuthenticatedUser();
		Project project = projectMapper.toEntity(request);
		project.setOwner(user);
		project.setStatus(ProjectStatus.PLANNING);
		Project savedProject = projectRepository.save(project);

		Member ownerMembership = new Member();
		ownerMembership.setProject(savedProject);
		ownerMembership.setUser(user);
		ownerMembership.setRole(MemberRole.OWNER);
		memberRepository.save(ownerMembership);
		activityLogService.log(savedProject, user, ProjectActivityType.PROJECT_CREATED, "Project created.");

		return toResponse(savedProject);
	}

	@Transactional(readOnly = true)
	public List<ProjectResponse> findAll() {
		var user = accessService.getAuthenticatedUser();

		return projectRepository.findAccessibleProjects(user.getId(), ProjectVisibility.PUBLIC)
			.stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public ProjectResponse findById(UUID projectId) {
		return toResponse(accessService.getProjectForView(projectId));
	}

	@Transactional
	public ProjectResponse update(UUID projectId, ProjectUpdateRequest request) {
		Project project = accessService.getProjectForProjectManagement(projectId);
		validateStatusTransition(project.getStatus(), request.status());
		projectMapper.updateEntity(project, request);
		activityLogService.log(project, accessService.getAuthenticatedUser(), ProjectActivityType.PROJECT_UPDATED, "Project updated.");

		return toResponse(project);
	}

	@Transactional
	public void delete(UUID projectId) {
		Project project = accessService.getProjectForProjectManagement(projectId);
		projectUpdateRepository.deleteAllByProjectId(project.getId());
		projectRepository.delete(project);
	}

	private ProjectResponse toResponse(Project project) {
		long totalTasks = taskRepository.countByProjectId(project.getId());
		long completedTasks = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
		return projectMapper.toResponse(project, totalTasks, completedTasks);
	}

	private void validateStatusTransition(ProjectStatus currentStatus, ProjectStatus targetStatus) {
		if (currentStatus == targetStatus) {
			return;
		}

		if (currentStatus == ProjectStatus.COMPLETED) {
			throw new ConflictException("Completed projects cannot change status.");
		}

		if (currentStatus == ProjectStatus.PLANNING && targetStatus == ProjectStatus.COMPLETED) {
			throw new ConflictException("Project cannot move directly from planning to completed.");
		}
	}
}

