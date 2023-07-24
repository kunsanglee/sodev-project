package dev.sodev.domain.likes.repository;

import dev.sodev.domain.likes.Likes;

public interface LikeCustomRepository {
    Likes isProjectLikes (Long id, Long projectId);
}
