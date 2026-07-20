package com.devnest.course.service.student;

import com.devnest.common.exception.ConflictException;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.course.dto.quiz.QuizAnswerResultResponse;
import com.devnest.course.dto.quiz.attempt.AttemptAnswerRequest;
import com.devnest.course.dto.quiz.attempt.AttemptResponse;
import com.devnest.course.dto.quiz.attempt.AttemptSubmitRequest;
import com.devnest.course.dto.student.quiz.QuizDetailsResponse;
import com.devnest.course.dto.student.quiz.QuizOptionResponse;
import com.devnest.course.dto.student.quiz.QuestionResponse;
import com.devnest.course.entity.quiz.Quiz;
import com.devnest.course.entity.quiz.QuizAnswer;
import com.devnest.course.entity.quiz.QuizAttempt;
import com.devnest.course.entity.quiz.option.Option;
import com.devnest.course.entity.quiz.QuizQuestion;
import com.devnest.course.repository.quiz.QuizAnswerRepository;
import com.devnest.course.repository.quiz.QuizAttemptRepository;
import com.devnest.course.repository.option.OptionRepository;
import com.devnest.course.repository.question.QuestionRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.devnest.course.service.course.CourseEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentQuizService {

	private final StudentLearningAccessService accessService;
	private final CourseEnrollmentService courseEnrollmentService;
	private final QuestionRepository questionRepository;
	private final OptionRepository optionRepository;
	private final QuizAttemptRepository quizAttemptRepository;
	private final QuizAnswerRepository quizAnswerRepository;

	@Transactional(readOnly = true)
	public QuizDetailsResponse getQuiz(UUID courseId, UUID lessonId) {
		var student = accessService.getAuthenticatedStudent();
		courseEnrollmentService.getActiveOrCompletedEnrollment(courseId);
		Quiz quiz = accessService.getPublishedCourseQuiz(courseId, lessonId);
		List<QuizQuestion> questions = questionRepository.findAllByQuizIdOrderByPositionAsc(quiz.getId());

		return new QuizDetailsResponse(
			quiz.getId(),
			quiz.getLesson().getId(),
			quiz.getTitle(),
			quiz.getPassingScore(),
			quiz.getMaxAttempts(),
			quiz.getMaxQuestions(),
			questions.stream().map(this::toStudentQuestionResponse).toList()
		);
	}

	@Transactional
	public AttemptResponse submitAttempt(UUID courseId, UUID lessonId, AttemptSubmitRequest request) {
		var student = accessService.getAuthenticatedStudent();
		courseEnrollmentService.getActiveOrCompletedEnrollment(courseId);
		Quiz quiz = accessService.getPublishedCourseQuiz(courseId, lessonId);
		List<QuizQuestion> questions = questionRepository.findAllByQuizIdOrderByPositionAsc(quiz.getId());

		validateAttemptRequest(quiz, questions, request.answers());

		int nextAttemptNumber = (int) quizAttemptRepository.countByQuizIdAndStudentId(quiz.getId(), student.getId()) + 1;
		QuizAttempt attempt = new QuizAttempt();
		attempt.setQuiz(quiz);
		attempt.setStudent(student);
		attempt.setAttemptNumber(nextAttemptNumber);
		attempt.setScore(0);
		attempt.setPassed(false);

		int correctAnswers = 0;
		QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

		for (AttemptAnswerRequest answerRequest : request.answers()) {
			QuizQuestion question = questions.stream()
				.filter(item -> item.getId().equals(answerRequest.questionId()))
				.findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Question not found."));

			Option selectedOption = optionRepository.findById(answerRequest.selectedOptionId())
				.orElseThrow(() -> new ResourceNotFoundException("Option not found."));

			if (!selectedOption.getQuestion().getId().equals(question.getId())) {
				throw new ConflictException("Selected option does not belong to the provided question.");
			}

			QuizAnswer answer = new QuizAnswer();
			answer.setAttempt(savedAttempt);
			answer.setQuestion(question);
			answer.setSelectedOption(selectedOption);
			answer.setCorrect(Boolean.TRUE.equals(selectedOption.getCorrect()));
			quizAnswerRepository.save(answer);

			if (Boolean.TRUE.equals(answer.getCorrect())) {
				correctAnswers++;
			}
		}

		int score = Math.round((correctAnswers * 100.0f) / questions.size());
		savedAttempt.setScore(score);
		savedAttempt.setPassed(score >= quiz.getPassingScore());

		return toAttemptResponse(savedAttempt);
	}

	@Transactional(readOnly = true)
	public List<AttemptResponse> findMyAttempts(UUID courseId, UUID lessonId) {
		var student = accessService.getAuthenticatedStudent();
		courseEnrollmentService.getActiveOrCompletedEnrollment(courseId);
		Quiz quiz = accessService.getPublishedCourseQuiz(courseId, lessonId);

		return quizAttemptRepository.findAllByQuizIdAndStudentIdOrderByAttemptNumberAsc(quiz.getId(), student.getId())
			.stream()
			.map(this::toAttemptResponse)
			.toList();
	}

	private void validateAttemptRequest(Quiz quiz, List<QuizQuestion> questions, List<AttemptAnswerRequest> answers) {
		if (quizAttemptRepository.countByQuizIdAndStudentId(quiz.getId(), accessService.getAuthenticatedStudent().getId()) >= quiz.getMaxAttempts()) {
			throw new ConflictException("Student has reached the maximum number of attempts for this quiz.");
		}

		if (questions.isEmpty()) {
			throw new ConflictException("Quiz has no questions.");
		}

		if (answers.size() != questions.size()) {
			throw new ConflictException("Student must answer all quiz questions exactly once.");
		}

		Set<UUID> uniqueQuestionIds = new HashSet<>();
		for (AttemptAnswerRequest answer : answers) {
			if (!uniqueQuestionIds.add(answer.questionId())) {
				throw new ConflictException("Student must answer each quiz question only once.");
			}
		}

		Set<UUID> validQuestionIds = questions.stream().map(QuizQuestion::getId).collect(java.util.stream.Collectors.toSet());
		for (AttemptAnswerRequest answer : answers) {
			if (!validQuestionIds.contains(answer.questionId())) {
				throw new ConflictException("Attempt contains a question that does not belong to this quiz.");
			}
		}
	}

	private QuestionResponse toStudentQuestionResponse(QuizQuestion question) {
		List<QuizOptionResponse> options = optionRepository.findAllByQuestionIdOrderByPositionAsc(question.getId())
			.stream()
			.map(option -> new QuizOptionResponse(option.getId(), option.getText(), option.getPosition()))
			.toList();

		return new QuestionResponse(
			question.getId(),
			question.getStatement(),
			question.getPosition(),
			options
		);
	}

	private AttemptResponse toAttemptResponse(QuizAttempt attempt) {
		Integer remainingAttempts = Math.max(attempt.getQuiz().getMaxAttempts() - attempt.getAttemptNumber(), 0);
		boolean reviewAvailable = Boolean.TRUE.equals(attempt.getPassed()) || remainingAttempts == 0;
		List<QuizAnswerResultResponse> answers = reviewAvailable
			? quizAnswerRepository.findAllByAttemptIdOrderByQuestionPositionAsc(attempt.getId())
				.stream()
				.map(answer -> new QuizAnswerResultResponse(
					answer.getQuestion().getId(),
					answer.getSelectedOption().getId(),
					answer.getCorrect()
				))
				.toList()
			: null;

		return new AttemptResponse(
			attempt.getId(),
			attempt.getQuiz().getId(),
			attempt.getStudent().getId(),
			attempt.getAttemptNumber(),
			attempt.getScore(),
			attempt.getPassed(),
			remainingAttempts,
			reviewAvailable,
			answers,
			attempt.getCreatedAt(),
			attempt.getUpdatedAt()
		);
	}
}

