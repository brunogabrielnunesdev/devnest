package com.devnest.project.repository.note;

import com.devnest.project.entity.note.Note;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, UUID> {

	List<Note> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId);

	Optional<Note> findByIdAndProjectId(UUID id, UUID projectId);
}
