package dev.sodev.domain.follow.repository;

import dev.sodev.domain.follow.Follow;
import dev.sodev.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    List<Follow> findAllByFromMember(Member fromMember);

    List<Follow> findAllByToMember(Member toMember);

    void deleteByFromMemberAndToMember(Member fromMember, Member toMember);

    Follow findByFromMemberAndToMember(Member fromMember, Member toMember);

}
