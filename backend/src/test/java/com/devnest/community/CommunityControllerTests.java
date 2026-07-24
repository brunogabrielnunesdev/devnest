package com.devnest.community;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devnest.admin.controller.community.CommunityForumAdminController;
import com.devnest.auth.security.jwt.JwtAuthenticationFilter;
import com.devnest.community.controller.forum.ForumController;
import com.devnest.community.controller.post.PostController;
import com.devnest.community.service.forum.ForumService;
import com.devnest.community.service.post.PostService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({
		ForumController.class,
		PostController.class,
		CommunityForumAdminController.class
})
@AutoConfigureMockMvc(addFilters = false)
class CommunityControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ForumService forumService;

	@MockitoBean
	private PostService postService;

	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Test
	@WithMockUser
	void listsForumsWithPagination() throws Exception {
		org.mockito.Mockito.when(forumService.findActive(any(Pageable.class))).thenReturn(Page.empty());

		mockMvc.perform(get("/community/forums").param("page", "0").param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());

		verify(forumService).findActive(any(Pageable.class));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCreatesForum() throws Exception {
		mockMvc.perform(post("/admin/community/forums")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "name": "Java",
							  "slug": "java",
							  "description": "Java discussions"
							}
							"""))
				.andExpect(status().isCreated());

		verify(forumService).create(any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void invalidForumSlugReturnsValidationError() throws Exception {
		mockMvc.perform(post("/admin/community/forums")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "name": "Java",
							  "slug": "Java Forum",
							  "description": "Java discussions"
							}
							"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.slug").exists());
	}

	@Test
	@WithMockUser
	void createsPostInForum() throws Exception {
		UUID forumId = UUID.randomUUID();

		mockMvc.perform(post("/community/forums/{forumId}/posts", forumId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "title": "My first post",
							  "content": "Post content",
							  "type": "DISCUSSION",
							  "tagIds": []
							}
							"""))
				.andExpect(status().isCreated());

		verify(postService).create(eq(forumId), any());
	}

	@Test
	@WithMockUser
	void blankPostFieldsReturnValidationErrors() throws Exception {
		mockMvc.perform(post("/community/forums/{forumId}/posts", UUID.randomUUID())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "title": "",
							  "content": "",
							  "type": null
							}
							"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.title").exists())
				.andExpect(jsonPath("$.fieldErrors.content").exists())
				.andExpect(jsonPath("$.fieldErrors.type").exists());
	}

	@Test
	@WithMockUser
	void removesPostWithNoContentResponse() throws Exception {
		UUID postId = UUID.randomUUID();

		mockMvc.perform(delete("/community/posts/{postId}", postId))
				.andExpect(status().isNoContent());

		verify(postService).remove(eq(postId), anyString());
	}
}
