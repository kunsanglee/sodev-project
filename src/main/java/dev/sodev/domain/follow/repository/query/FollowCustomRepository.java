package dev.sodev.domain.follow.repository.query;

import dev.sodev.domain.member.Member;

public interface FollowCustomRepository {

    void findFollowAndDelete(Member member);
}
