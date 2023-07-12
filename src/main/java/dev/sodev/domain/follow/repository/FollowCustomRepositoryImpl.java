package dev.sodev.domain.follow.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static dev.sodev.domain.Follow.QFollow.follow;


@RequiredArgsConstructor
@Repository
public class FollowCustomRepositoryImpl implements FollowCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public void findFollowAndDelete(Member fromMember, Member toMember) {
        queryFactory
                .delete(follow)
                .where(follow.fromMember.eq(fromMember).and(follow.toMember.eq(toMember)))
                .execute();
    }
}
