package com.devnest.community.service.content;

import com.devnest.community.config.CommunityLimitsProperties;
import com.devnest.community.entity.comment.Comment;
import com.devnest.community.entity.post.Post;
import com.devnest.community.exception.duplicate.DuplicateContentException;
import com.devnest.community.repository.comment.CommentRepository;
import com.devnest.community.repository.post.PostRepository;
import java.text.Normalizer;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DuplicateContentService {

	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final CommunityLimitsProperties limits;
	private final Clock communityClock;

	public DuplicateContentService(
			PostRepository postRepository,
			CommentRepository commentRepository,
			CommunityLimitsProperties limits,
			Clock communityClock
	) {
		this.postRepository = postRepository;
		this.commentRepository = commentRepository;
		this.limits = limits;
		this.communityClock = communityClock;
	}

	public void validatePost(UUID authorId, String title, String content) {
		String fingerprint = normalize(title + "\n" + content);
		boolean duplicate = postRepository
				.findAllByAuthorIdAndCreatedAtGreaterThanEqual(authorId, windowStart())
				.stream()
				.map(this::postFingerprint)
				.anyMatch(fingerprint::equals);
		if (duplicate) {
			throw new DuplicateContentException("post");
		}
	}

	public void validateComment(UUID authorId, String content) {
		String fingerprint = normalize(content);
		boolean duplicate = commentRepository
				.findAllByAuthorIdAndCreatedAtGreaterThanEqual(authorId, windowStart())
				.stream()
				.map(Comment::getContent)
				.map(this::normalize)
				.anyMatch(fingerprint::equals);
		if (duplicate) {
			throw new DuplicateContentException("comment");
		}
	}

	private OffsetDateTime windowStart() {
		return OffsetDateTime.now(communityClock)
				.minusMinutes(limits.getDuplicateContentWindowMinutes());
	}

	private String postFingerprint(Post post) {
		return normalize(post.getTitle() + "\n" + post.getContent());
	}

	private String normalize(String value) {
		String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
				.toLowerCase(Locale.ROOT);
		return Normalizer.normalize(normalized, Normalizer.Form.NFD)
				.replaceAll("\\p{M}+", "")
				.replaceAll("[^\\p{L}\\p{N}]+", " ")
				.trim()
				.replaceAll("\\s+", " ");
	}
}
