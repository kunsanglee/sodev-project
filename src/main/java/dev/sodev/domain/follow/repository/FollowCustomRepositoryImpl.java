package dev.sodev.domain.follow.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static dev.sodev.domain.follow.QFollow.follow;


@RequiredArgsConstructor
@Repository
public class FollowCustomRepositoryImpl implements FollowCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public void findFollowAndDelete(Member member) {
        queryFactory
                .delete(follow)
                .where(follow.fromMember.eq(member).or(follow.toMember.eq(member)))
                .execute();
    }
}
