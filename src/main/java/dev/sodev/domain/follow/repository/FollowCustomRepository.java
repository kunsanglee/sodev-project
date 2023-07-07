package dev.sodev.domain.follow.repository;

import dev.sodev.domain.member.Member;

public interface FollowCustomRepository {

    void findFollowAndDelete(Member fromMember, Member toMember);
}
