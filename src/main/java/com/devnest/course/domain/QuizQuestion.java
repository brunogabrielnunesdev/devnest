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
	name = "quiz_questions",
	uniqueConstraints = @UniqueConstraint(name = "uk_quiz_questions_quiz_position", columnNames = {"quiz_id", "position"})
)
@NoArgsConstructor
public class QuizQuestion extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "quiz_id", nullable = false)
	private Quiz quiz;

	@Column(nullable = false, columnDefinition = "text")
	private String statement;

	@Column(nullable = false)
	private Integer position;
}
