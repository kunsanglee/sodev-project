package dev.sodev.domain.likes.repository;

import dev.sodev.domain.likes.Likes;
import dev.sodev.domain.likes.dto.LikesMemberDto;
import dev.sodev.domain.likes.dto.LikesProjectDto;
import dev.sodev.domain.project.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface LikeCustomRepository {
    Likes isProjectLikes (Long id, Long projectId);
    List<LikesMemberDto> likeList(Long projectId);

    Slice<LikesProjectDto> findLikedProjectsByMemberId(Long memberId, Pageable pageable);
}
