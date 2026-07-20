package com.devnest.course.repository.comment;

import com.devnest.course.entity.comment.CommentStatus;
import com.devnest.course.entity.comment.Comment;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

	java.util.List<Comment> findAllByLessonIdAndStatusAndHiddenFalseOrderByCreatedAtDesc(UUID lessonId, CommentStatus status);

	java.util.List<Comment> findAllByStatusInOrderByCreatedAtDesc(java.util.List<CommentStatus> statuses);

	long countByLessonModuleCourseTeacherId(UUID teacherId);

	long countByStudentId(UUID studentId);

	@Query("""
		select avg(lc.rating)
		from Comment lc
		where lc.lesson.module.course.teacher.id = :teacherId
		""")
	Double findAverageRatingByTeacherId(@Param("teacherId") UUID teacherId);

	void deleteAllByLessonModuleCourseId(UUID courseId);

	@Query("""
		select lc
		from Comment lc
		where (:query is null or lower(lc.content) like lower(concat('%', :query, '%')))
		order by lc.createdAt desc
		""")
	Page<Comment> findAdminComments(@Param("query") String query, Pageable pageable);

	@Query("""
		select lc
		from Comment lc
		join fetch lc.lesson lesson
		join fetch lesson.module module
		join fetch module.course course
		join fetch lc.student student
		left join fetch student.profile profile
		where (:query is null or lower(lc.content) like lower(concat('%', :query, '%')))
		order by lc.createdAt desc
		""")
	java.util.List<Comment> findAllAdminComments(@Param("query") String query);
}

