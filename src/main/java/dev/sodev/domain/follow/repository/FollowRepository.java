package dev.sodev.domain.follow.repository;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import dev.sodev.domain.follow.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface FollowRepository extends JpaRepository<Follow, Long> {

    Slice<Follow> findAllByToMember_Id(Long memberId, Pageable pageable); // 팔로워 조회

    Slice<Follow> findAllByFromMember_Id(Long memberId, Pageable pageable); // 팔로잉 조회

    @Modifying
    @Query("delete from Follow f where f.fromMember.id = :memberId or f.toMember.id = :memberId")
    void deleteAllByMemberId(Long memberId);

}
