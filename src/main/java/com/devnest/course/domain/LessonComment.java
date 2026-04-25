package com.devnest.course.domain;

import com.devnest.shared.domain.BaseEntity;
import com.devnest.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "lesson_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LessonComment extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "lesson_id", nullable = false)
	private Lesson lesson;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private User student;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@Column(nullable = false)
	private Integer rating;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private CommentStatus status;

	@Column(name = "moderation_reason")
	private String moderationReason;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "removed_by")
	private User removedBy;

	@Column(name = "removed_at")
	private OffsetDateTime removedAt;
}
