package com.devnest.course.entity.quiz.option;

import com.devnest.common.entity.BaseEntity;
import com.devnest.course.entity.quiz.QuizQuestion;
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
	name = "quiz_options",
	uniqueConstraints = @UniqueConstraint(name = "uk_quiz_options_question_position", columnNames = {"question_id", "position"})
)
@NoArgsConstructor
public class Option extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	private QuizQuestion question;

	@Column(nullable = false, columnDefinition = "text")
	private String text;

	@Column(name = "is_correct", nullable = false)
	private Boolean correct;

	@Column(nullable = false)
	private Integer position;
}

