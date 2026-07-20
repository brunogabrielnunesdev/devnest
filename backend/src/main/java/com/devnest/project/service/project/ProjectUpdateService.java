package com.devnest.project.service.project;

import com.devnest.project.entity.project.Project;
import com.devnest.project.entity.project.ProjectUpdate;
import com.devnest.project.dto.project.updateproject.ProjectUpdateResponse;
import com.devnest.project.mapper.ProjectUpdateMapper;
import com.devnest.project.repository.project.ProjectUpdateRepository;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectUpdateService {

	private final ProjectAccessService accessService;
	private final ProjectUpdateMapper projectUpdateMapper;
	private final ProjectUpdateRepository projectUpdateRepository;

	@Transactional
	public ProjectUpdateResponse create(UUID projectId, ProjectUpdate projectUpdate) {
		Project project = accessService.getProjectForContentManagement(projectId);
		projectUpdate.setProject(project);

		return projectUpdateMapper.toResponse(projectUpdateRepository.save(projectUpdate));
	}

	@Transactional(readOnly = true)
	public List<ProjectUpdateResponse> findAll(UUID projectId) {
		Project project = accessService.getProjectForView(projectId);

		return projectUpdateRepository.findAllByProjectIdOrderByCreatedAtDesc(project.getId())
			.stream()
			.map(projectUpdateMapper::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public ProjectUpdateResponse findById(UUID projectId, UUID updateId) {
		return projectUpdateMapper.toResponse(accessService.getProjectUpdateForView(projectId, updateId));
	}

	@Transactional
	public ProjectUpdateResponse update(UUID projectId, UUID updateId, ProjectUpdate updateData) {
		ProjectUpdate projectUpdate = accessService.getProjectUpdateForManagement(projectId, updateId);
		projectUpdate.setTitle(updateData.getTitle());
		projectUpdate.setContent(updateData.getContent());

		return projectUpdateMapper.toResponse(projectUpdate);
	}

	@Transactional
	public void delete(UUID projectId, UUID updateId) {
		ProjectUpdate projectUpdate = accessService.getProjectUpdateForManagement(projectId, updateId);
		projectUpdateRepository.delete(projectUpdate);
	}
}

