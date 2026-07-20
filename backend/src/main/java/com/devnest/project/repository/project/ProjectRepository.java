package com.devnest.project.repository.project;

import com.devnest.project.entity.project.Project;
import com.devnest.project.entity.project.ProjectVisibility;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

	List<Project> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

	Optional<Project> findByIdAndOwnerId(UUID id, UUID ownerId);

	@Query("""
		select distinct p
		from Project p
		left join p.members m
		where p.owner.id = :userId
			or m.user.id = :userId
			or p.visibility = :visibility
		order by p.createdAt desc
	""")
	List<Project> findAccessibleProjects(
		@Param("userId") UUID userId,
		@Param("visibility") ProjectVisibility visibility
	);
}

