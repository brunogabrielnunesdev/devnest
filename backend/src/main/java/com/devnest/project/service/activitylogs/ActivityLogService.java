package com.devnest.project.service.activitylogs;

import com.devnest.identity.entity.User;
import com.devnest.project.entity.project.Project;
import com.devnest.project.entity.activitylogs.ActivityLog;
import com.devnest.project.entity.activitylogs.ProjectActivityType;
import com.devnest.project.repository.activitylogs.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

	private final ActivityLogRepository activityLogRepository;

	public void log(Project project, User actor, ProjectActivityType type, String message) {
		ActivityLog log = new ActivityLog();
		log.setProject(project);
		log.setActor(actor);
		log.setType(type);
		log.setMessage(message);
		activityLogRepository.save(log);
	}
}
