package dev.sodev.domain.likes.repository;


import dev.sodev.domain.likes.Likes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface LikeRepository extends JpaRepository<Likes, Long>, LikeCustomRepository {

    Long countLikesByProjectId(Long projectId);

}
