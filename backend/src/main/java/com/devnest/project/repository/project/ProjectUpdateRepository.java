package com.devnest.project.repository.project;

import com.devnest.project.entity.project.ProjectUpdate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectUpdateRepository extends JpaRepository<ProjectUpdate, UUID> {

	List<ProjectUpdate> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId);

	Optional<ProjectUpdate> findByIdAndProjectId(UUID id, UUID projectId);

	void deleteAllByProjectId(UUID projectId);
}

