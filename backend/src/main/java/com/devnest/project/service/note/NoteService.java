package com.devnest.project.service.note;

import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.project.dto.note.NoteCreateRequest;
import com.devnest.project.dto.note.NoteResponse;
import com.devnest.project.dto.note.NoteUpdateRequest;
import com.devnest.project.entity.project.Project;
import com.devnest.project.entity.activitylogs.ProjectActivityType;
import com.devnest.project.entity.note.Note;
import com.devnest.project.mapper.ProjectMapper;
import com.devnest.project.repository.note.NoteRepository;
import java.util.List;
import java.util.UUID;

import com.devnest.project.service.activitylogs.ActivityLogService;
import com.devnest.project.service.project.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteService {

	private final ProjectAccessService accessService;
	private final ActivityLogService activityLogService;
	private final ProjectMapper projectMapper;
	private final NoteRepository noteRepository;

	@Transactional
	public NoteResponse create(UUID projectId, NoteCreateRequest request) {
		Project project = accessService.getProjectForContentManagement(projectId);
		Note note = new Note();
		note.setProject(project);
		note.setAuthor(accessService.getAuthenticatedUser());
		note.setContent(request.content());
		Note savedNote = noteRepository.save(note);

		activityLogService.log(project, note.getAuthor(), ProjectActivityType.NOTE_CREATED, "Note created.");
		return toResponse(savedNote);
	}

	@Transactional(readOnly = true)
	public List<NoteResponse> findAll(UUID projectId) {
		Project project = accessService.getProjectForView(projectId);
		return noteRepository.findAllByProjectIdOrderByCreatedAtDesc(project.getId())
			.stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional
	public NoteResponse update(UUID projectId, UUID noteId, NoteUpdateRequest request) {
		Note note = getManagedNote(projectId, noteId);
		note.setContent(request.content());
		activityLogService.log(note.getProject(), accessService.getAuthenticatedUser(), ProjectActivityType.NOTE_UPDATED, "Note updated.");
		return toResponse(note);
	}

	@Transactional
	public void delete(UUID projectId, UUID noteId) {
		Note note = getManagedNote(projectId, noteId);
		activityLogService.log(note.getProject(), accessService.getAuthenticatedUser(), ProjectActivityType.NOTE_DELETED, "Note removed.");
		noteRepository.delete(note);
	}

	private Note getManagedNote(UUID projectId, UUID noteId) {
		Project project = accessService.getProjectForContentManagement(projectId);
		return noteRepository.findByIdAndProjectId(noteId, project.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Project note not found."));
	}

	private NoteResponse toResponse(Note note) {
		return new NoteResponse(
			note.getId(),
			note.getProject().getId(),
			projectMapper.toUserSummary(note.getAuthor()),
			note.getContent(),
			note.getCreatedAt(),
			note.getUpdatedAt()
		);
	}
}
