package com.devnest.project.repository.activitylogs;

import com.devnest.project.entity.activitylogs.ActivityLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

	List<ActivityLog> findTop20ByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
