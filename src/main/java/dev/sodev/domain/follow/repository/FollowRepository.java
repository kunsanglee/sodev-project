package dev.sodev.domain.follow.repository;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import dev.sodev.domain.follow.Follow;
import dev.sodev.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FollowRepository extends JpaRepository<Follow, Long> {

    Follow findByFromMemberAndToMember(Member fromMember, Member toMember);

    Slice<Follow> findAllByToMember_Id(Long memberId, Pageable pageable); // 팔로워 조회

    Slice<Follow> findAllByFromMember_Id(Long memberId, Pageable pageable); // 팔로잉 조회

}
