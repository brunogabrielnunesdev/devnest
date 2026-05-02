package com.devnest.course.domain;

import com.devnest.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "quizzes")
@NoArgsConstructor
public class Quiz extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "lesson_id", nullable = false, unique = true)
	private Lesson lesson;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(name = "passing_score", nullable = false)
	private Integer passingScore;

	@Column(name = "max_attempts", nullable = false)
	private Integer maxAttempts;

	@Column(name = "max_questions", nullable = false)
	private Integer maxQuestions;
}
