package dev.sodev.domain.likes.service;

import dev.sodev.domain.likes.Likes;
import dev.sodev.domain.likes.dto.response.LikeResponse;
import dev.sodev.domain.likes.repository.LikeCustomRepository;
import dev.sodev.domain.likes.repository.LikeRepository;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.security.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
@Transactional
@Service
@RequiredArgsConstructor
public class LikeService {
    private final ProjectRepository projectRepository;
    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;

    public LikeResponse like(Long projectId) {

        // 로그인하지않은경우 에러반환
        if(SecurityUtil.getMemberEmail()==null) throw new SodevApplicationException(ErrorCode.UNAUTHORIZED_USER);
        // 좋아요 할 프로젝트가 없을경우 에러반환
        Project project = projectRepository.findById(projectId).orElseThrow( () -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
        // like 테이블에 값이 없으면 추가 있으면 삭제
        Member member = memberRepository.getReferenceByEmail(SecurityUtil.getMemberEmail());
        if(project.getCreatedBy().equals(SecurityUtil.getMemberEmail()))
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "본인이 작성한 게시물은 좋아요를 누를 수 없습니다.");

        LikeResponse likeResponse = new LikeResponse();
        Likes likes = likeRepository.isProjectLikes(member.getId(), projectId);
        if(likes==null) {
            likeRepository.save(Likes.of(member, project));
            likeResponse.setMessage(String.format("%s 을(를) 관심프로젝트에 저장하였습니다.", project.getTitle()));
        } else {
            likeRepository.delete(likes);
            likeResponse.setMessage(String.format("%s 을(를) 관심프로젝트에서 삭제하였습니다.", project.getTitle()));
        }
        return likeResponse;
    }

}
