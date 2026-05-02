package com.devnest.course.domain;

import com.devnest.shared.domain.BaseEntity;
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
	name = "quiz_options",
	uniqueConstraints = @UniqueConstraint(name = "uk_quiz_options_question_position", columnNames = {"question_id", "position"})
)
@NoArgsConstructor
public class QuizOption extends BaseEntity {

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
