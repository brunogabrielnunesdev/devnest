package com.devnest.course.repository;

import com.devnest.course.domain.QuizOption;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizOptionRepository extends JpaRepository<QuizOption, UUID> {

	List<QuizOption> findAllByQuestionIdOrderByPositionAsc(UUID questionId);

	boolean existsByQuestionId(UUID questionId);

	void deleteAllByQuestionId(UUID questionId);
}
