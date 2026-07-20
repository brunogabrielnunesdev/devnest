package com.devnest.course.repository.quiz;

import com.devnest.course.entity.quiz.QuizAttempt;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

	List<QuizAttempt> findAllByQuizIdAndStudentIdOrderByAttemptNumberAsc(UUID quizId, UUID studentId);

	Optional<QuizAttempt> findTopByQuizIdAndStudentIdOrderByAttemptNumberDesc(UUID quizId, UUID studentId);

	long countByQuizIdAndStudentId(UUID quizId, UUID studentId);

	boolean existsByQuizIdAndStudentIdAndPassedTrue(UUID quizId, UUID studentId);

	@Query("""
		select count(distinct qa.quiz.id)
		from QuizAttempt qa
		where qa.student.id = :studentId
		""")
	long countDistinctQuizIdByStudentId(@Param("studentId") UUID studentId);

	@Query("""
		select avg(qa.score)
		from QuizAttempt qa
		where qa.student.id = :studentId
		""")
	Double findAverageScoreByStudentId(@Param("studentId") UUID studentId);

	void deleteAllByQuizLessonModuleCourseId(UUID courseId);
}

