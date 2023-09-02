package dev.sodev.domain.likes.service;

import dev.sodev.domain.likes.Likes;
import dev.sodev.domain.likes.dto.response.LikeResponse;
import dev.sodev.domain.likes.repository.LikeRepository;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.security.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Transactional
@Service
@RequiredArgsConstructor
public class LikeService {

    private final ProjectRepository projectRepository;
    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;

    public LikeResponse like(Long projectId) {
        String memberEmail = isLoginMember();
        Project project = getProjectById(projectId);
        Member member = getReferenceByEmail(memberEmail);

        if (project.getCreatedBy().equals(memberEmail)) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "본인이 작성한 게시물은 좋아요를 누를 수 없습니다.");
        }

        return addOrDelete(projectId, project, member);
    }

    // like 테이블에 값이 없으면 추가 있으면 삭제.
    private LikeResponse addOrDelete(Long projectId, Project project, Member member) {
        Likes likes = likeRepository.isProjectLikes(member.getId(), projectId);
        if (likes == null) {
            likeRepository.save(Likes.of(member, project));
            return LikeResponse.builder()
                    .message(String.format("%s 을(를) 관심프로젝트에 저장하였습니다.", project.getTitle()))
                    .build();
        }
        likeRepository.delete(likes);
        return LikeResponse.builder()
                .message(String.format("%s 을(를) 관심프로젝트에서 삭제하였습니다.", project.getTitle()))
                .build();
    }

    // 회원의 이메일로 조회.
    private Member getReferenceByEmail(String memberEmail) {
        return memberRepository.getReferenceByEmail(memberEmail);
    }

    // 로그인하지않은경우 에러반환.
    private static String isLoginMember() {
        String memberEmail = SecurityUtil.getMemberEmail();
        if(memberEmail == null) throw new SodevApplicationException(ErrorCode.UNAUTHORIZED_USER);
        return memberEmail;
    }

    // 좋아요 할 프로젝트가 없을경우 에러반환.
    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
    }

}
