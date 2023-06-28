package dev.sodev.repository;


import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.entity.Member;
import dev.sodev.domain.enums.Auth;
import dev.sodev.domain.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static dev.sodev.domain.entity.QMember.*;

@Slf4j
@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    JPAQueryFactory queryFactory;
    @Autowired MemberRepository memberRepository;


    @BeforeEach
    public void createMember() {
        Member member = Member.builder()
                .userName("testUser")
                .pwd("1234")
                .nickName("testNickname")
                .auth(Auth.MEMBER)
                .introduce("테스트 소개입니다.")
                .build();
        memberRepository.save(member);
    }

    @Test
    void find() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.id.eq(1L))
                .fetchOne();

        log.info("member.userName={}", findMember.getUserName());
        Assertions.assertEquals("testUser", findMember.getUserName());

    }
}