package com.devnest.project.repository.task;

import com.devnest.project.entity.task.ProjectTask;
import com.devnest.project.entity.task.TaskStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<ProjectTask, UUID> {

	List<ProjectTask> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId);

	Optional<ProjectTask> findByIdAndProjectId(UUID id, UUID projectId);

	long countByProjectId(UUID projectId);

	long countByProjectIdAndStatus(UUID projectId, TaskStatus status);
}
