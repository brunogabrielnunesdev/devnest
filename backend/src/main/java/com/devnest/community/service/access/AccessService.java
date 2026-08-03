package com.devnest.community.service.access;

import com.devnest.auth.security.useridentity.CustomUserProvider;
import com.devnest.community.entity.comment.Comment;
import com.devnest.community.entity.post.Post;
import com.devnest.community.exception.access.CommunityForbiddenException;
import com.devnest.community.exception.comment.CommentNotFoundException;
import com.devnest.community.exception.post.PostNotFoundException;
import com.devnest.community.repository.comment.CommentRepository;
import com.devnest.community.repository.post.PostRepository;
import com.devnest.identity.entity.User;
import com.devnest.identity.entity.UserRole;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessService {

	private final CustomUserProvider customUserProvider;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;

	public User getAuthenticatedUser() {
		return customUserProvider.getAuthenticatedUser();
	}

	public User getAuthenticatedAdmin() {
		return getAuthenticatedAdmin("Only admins can manage community forums.");
	}

	public User getAuthenticatedModerator() {
		return getAuthenticatedAdmin("Only admins can manage community resources.");
	}

	private User getAuthenticatedAdmin(String message) {
		User user = getAuthenticatedUser();
		if (user.getRole() != UserRole.ADMIN) {
			throw new CommunityForbiddenException(message);
		}
		return user;
	}

	public Post getPostForManagement(UUID postId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(PostNotFoundException::new);
		User user = getAuthenticatedUser();
		if (user.getRole() != UserRole.ADMIN && !post.getAuthor().getId().equals(user.getId())) {
			throw new CommunityForbiddenException("Only the post author or an admin can manage this post.");
		}
		return post;
	}

	public Comment getCommentForManagement(UUID commentId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(CommentNotFoundException::new);
		User user = getAuthenticatedUser();
		if (user.getRole() != UserRole.ADMIN && !comment.getAuthor().getId().equals(user.getId())) {
			throw new CommunityForbiddenException("Only the comment author or an admin can manage this comment.");
		}
		return comment;
	}
}
