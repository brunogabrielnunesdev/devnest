package com.devnest.course.domain;

import com.devnest.shared.domain.BaseEntity;
import com.devnest.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
	name = "quiz_attempts",
	uniqueConstraints = @UniqueConstraint(name = "uk_quiz_attempts_student_quiz_number", columnNames = {"student_id", "quiz_id", "attempt_number"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAttempt extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "quiz_id", nullable = false)
	private Quiz quiz;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private User student;

	@Column(name = "attempt_number", nullable = false)
	private Integer attemptNumber;

	@Column(nullable = false)
	private Integer score;

	@Column(nullable = false)
	private Boolean passed;
}
