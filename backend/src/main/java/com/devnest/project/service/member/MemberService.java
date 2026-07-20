package com.devnest.project.service.member;

import com.devnest.common.exception.ConflictException;
import com.devnest.common.exception.ResourceNotFoundException;
import com.devnest.identity.repository.UserRepository;
import com.devnest.project.dto.members.MemberCreateRequest;
import com.devnest.project.dto.members.MemberResponse;
import com.devnest.project.dto.members.MemberUpdateRequest;
import com.devnest.project.entity.project.Project;
import com.devnest.project.entity.activitylogs.ProjectActivityType;
import com.devnest.project.entity.member.Member;
import com.devnest.project.entity.member.MemberRole;
import com.devnest.project.mapper.ProjectMapper;
import com.devnest.project.repository.member.MemberRepository;
import java.util.List;
import java.util.UUID;

import com.devnest.project.service.activitylogs.ActivityLogService;
import com.devnest.project.service.project.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final ProjectAccessService accessService;
	private final ActivityLogService activityLogService;
	private final ProjectMapper projectMapper;
	private final MemberRepository memberRepository;
	private final UserRepository userRepository;

	@Transactional
	public MemberResponse create(UUID projectId, MemberCreateRequest request) {
		Project project = accessService.getProjectForContentManagement(projectId);
		validateRequestedRole(request.role());

		if (project.getOwner().getId().equals(request.userId())) {
			throw new ConflictException("Project owner is already part of the project.");
		}

		if (memberRepository.existsByProjectIdAndUserId(project.getId(), request.userId())) {
			throw new ConflictException("User is already a member of this project.");
		}

		Member member = new Member();
		member.setProject(project);
		member.setUser(userRepository.findById(request.userId())
			.orElseThrow(() -> new ResourceNotFoundException("User not found.")));
		member.setRole(request.role());
		Member savedMember = memberRepository.save(member);

		activityLogService.log(
			project,
			accessService.getAuthenticatedUser(),
			ProjectActivityType.MEMBER_ADDED,
			"Member added: " + savedMember.getUser().getEmail()
		);
		return toResponse(savedMember);
	}

	@Transactional(readOnly = true)
	public List<MemberResponse> findAll(UUID projectId) {
		Project project = accessService.getProjectForView(projectId);
		return memberRepository.findAllByProjectIdOrderByCreatedAtAsc(project.getId())
			.stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional
	public MemberResponse update(UUID projectId, UUID memberId, MemberUpdateRequest request) {
		validateRequestedRole(request.role());
		Member member = getManagedMember(projectId, memberId);

		if (member.getRole() == MemberRole.OWNER) {
			throw new ConflictException("Owner membership cannot be changed.");
		}

		member.setRole(request.role());
		activityLogService.log(
			member.getProject(),
			accessService.getAuthenticatedUser(),
			ProjectActivityType.MEMBER_UPDATED,
			"Member updated: " + member.getUser().getEmail()
		);
		return toResponse(member);
	}

	@Transactional
	public void delete(UUID projectId, UUID memberId) {
		Member member = getManagedMember(projectId, memberId);
		if (member.getRole() == MemberRole.OWNER) {
			throw new ConflictException("Owner membership cannot be removed.");
		}

		activityLogService.log(
			member.getProject(),
			accessService.getAuthenticatedUser(),
			ProjectActivityType.MEMBER_REMOVED,
			"Member removed: " + member.getUser().getEmail()
		);
		memberRepository.delete(member);
	}

	private Member getManagedMember(UUID projectId, UUID memberId) {
		Project project = accessService.getProjectForContentManagement(projectId);
		return memberRepository.findByIdAndProjectId(memberId, project.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Project member not found."));
	}

	private void validateRequestedRole(MemberRole role) {
		if (role == MemberRole.OWNER) {
			throw new ConflictException("Owner role is reserved for the project owner.");
		}
	}

	private MemberResponse toResponse(Member member) {
		return new MemberResponse(
			member.getId(),
			member.getProject().getId(),
			projectMapper.toUserSummary(member.getUser()),
			member.getRole(),
			member.getCreatedAt()
		);
	}
}
