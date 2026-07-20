package com.devnest.admin.service.metrics;

import com.devnest.admin.dto.metrics.MetricsResponse;
import com.devnest.admin.service.acess.AccessService;
import com.devnest.course.repository.course.CourseRepository;
import com.devnest.course.repository.comment.CommentRepository;
import com.devnest.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminMetricsService")
@RequiredArgsConstructor
public class MetricsService {

	private final AccessService accessService;
	private final UserRepository userRepository;
	private final CourseRepository courseRepository;
	private final CommentRepository commentRepository;

	@Transactional(readOnly = true)
	public MetricsResponse getMetrics() {
		accessService.getAuthenticatedAdmin();
		return new MetricsResponse(
			userRepository.count(),
			courseRepository.count(),
			commentRepository.count()
		);
	}
}
