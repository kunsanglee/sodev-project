package dev.sodev.domain.likes.repository;

import dev.sodev.domain.likes.Likes;
import dev.sodev.domain.likes.dto.LikesDto;

import java.util.List;

public interface LikeCustomRepository {
    Likes isProjectLikes (Long id, Long projectId);
    List<LikesDto> likeList(Long projectId);
}
