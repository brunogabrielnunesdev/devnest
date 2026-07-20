package com.devnest.project.service.task;

import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.identity.entity.User;
import com.devnest.identity.repository.UserRepository;
import com.devnest.project.dto.task.TaskCreateRequest;
import com.devnest.project.dto.task.TaskResponse;
import com.devnest.project.dto.task.TaskUpdateRequest;
import com.devnest.project.entity.project.Project;
import com.devnest.project.entity.activitylogs.ProjectActivityType;
import com.devnest.project.entity.task.ProjectTask;
import com.devnest.project.entity.task.TaskStatus;
import com.devnest.project.mapper.ProjectMapper;
import com.devnest.project.repository.task.TaskRepository;
import java.util.List;
import java.util.UUID;

import com.devnest.project.service.activitylogs.ActivityLogService;
import com.devnest.project.service.project.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

	private final ProjectAccessService accessService;
	private final ActivityLogService activityLogService;
	private final ProjectMapper projectMapper;
	private final TaskRepository taskRepository;
	private final UserRepository userRepository;

	@Transactional
	public TaskResponse create(UUID projectId, TaskCreateRequest request) {
		Project project = accessService.getProjectForContentManagement(projectId);
		ProjectTask task = new ProjectTask();
		task.setProject(project);
		task.setTitle(request.title());
		task.setDescription(request.description());
		task.setStatus(request.status());
		task.setPriority(request.priority());
		task.setAssignedTo(findAssignee(request.assignedToId()));
		task.setDueDate(request.dueDate());
		ProjectTask savedTask = taskRepository.save(task);

		User actor = accessService.getAuthenticatedUser();
		activityLogService.log(project, actor, ProjectActivityType.TASK_CREATED, "Task created: " + savedTask.getTitle());
		if (savedTask.getStatus() == TaskStatus.DONE) {
			activityLogService.log(project, actor, ProjectActivityType.TASK_DONE, "Task completed: " + savedTask.getTitle());
		}

		return toResponse(savedTask);
	}

	@Transactional(readOnly = true)
	public List<TaskResponse> findAll(UUID projectId) {
		Project project = accessService.getProjectForView(projectId);
		return taskRepository.findAllByProjectIdOrderByCreatedAtDesc(project.getId())
			.stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional
	public TaskResponse update(UUID projectId, UUID taskId, TaskUpdateRequest request) {
		ProjectTask task = getManagedTask(projectId, taskId);
		TaskStatus previousStatus = task.getStatus();
		task.setTitle(request.title());
		task.setDescription(request.description());
		task.setStatus(request.status());
		task.setPriority(request.priority());
		task.setAssignedTo(findAssignee(request.assignedToId()));
		task.setDueDate(request.dueDate());

		User actor = accessService.getAuthenticatedUser();
		activityLogService.log(task.getProject(), actor, ProjectActivityType.TASK_UPDATED, "Task updated: " + task.getTitle());
		if (previousStatus != TaskStatus.DONE && task.getStatus() == TaskStatus.DONE) {
			activityLogService.log(task.getProject(), actor, ProjectActivityType.TASK_DONE, "Task completed: " + task.getTitle());
		}

		return toResponse(task);
	}

	@Transactional
	public void delete(UUID projectId, UUID taskId) {
		ProjectTask task = getManagedTask(projectId, taskId);
		activityLogService.log(
			task.getProject(),
			accessService.getAuthenticatedUser(),
			ProjectActivityType.TASK_DELETED,
			"Task removed: " + task.getTitle()
		);
		taskRepository.delete(task);
	}

	private ProjectTask getManagedTask(UUID projectId, UUID taskId) {
		Project project = accessService.getProjectForContentManagement(projectId);
		return taskRepository.findByIdAndProjectId(taskId, project.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Project task not found."));
	}

	private User findAssignee(UUID userId) {
		if (userId == null) {
			return null;
		}

		return userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("Assigned user not found."));
	}

	private TaskResponse toResponse(ProjectTask task) {
		return new TaskResponse(
			task.getId(),
			task.getProject().getId(),
			task.getTitle(),
			task.getDescription(),
			task.getStatus(),
			task.getPriority(),
			task.getAssignedTo() == null ? null : projectMapper.toUserSummary(task.getAssignedTo()),
			task.getDueDate(),
			task.getCreatedAt(),
			task.getUpdatedAt()
		);
	}
}
