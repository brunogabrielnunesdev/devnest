package com.devnest.course.entity.quiz;

import com.devnest.common.entity.BaseEntity;
import com.devnest.course.entity.quiz.option.Option;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
	name = "quiz_answers",
	uniqueConstraints = @UniqueConstraint(name = "uk_quiz_answers_attempt_question", columnNames = {"attempt_id", "question_id"})
)
@NoArgsConstructor
public class QuizAnswer extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "attempt_id", nullable = false)
	private QuizAttempt attempt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	private QuizQuestion question;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "selected_option_id", nullable = false)
	private Option selectedOption;

	@Column(nullable = false)
	private Boolean correct;
}

