package com.devnest.course.repository.option;

import com.devnest.course.entity.quiz.option.Option;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository extends JpaRepository<Option, UUID> {

	List<Option> findAllByQuestionIdOrderByPositionAsc(UUID questionId);

	boolean existsByQuestionId(UUID questionId);

	boolean existsByQuestionIdAndPosition(UUID questionId, Integer position);

	boolean existsByQuestionIdAndPositionAndIdNot(UUID questionId, Integer position, UUID id);

	void deleteAllByQuestionId(UUID questionId);

	void deleteAllByQuestionQuizLessonModuleCourseId(UUID courseId);
}

