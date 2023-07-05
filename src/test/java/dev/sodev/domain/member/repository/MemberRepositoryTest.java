package dev.sodev.domain.member.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.member.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static dev.sodev.domain.member.QMember.member;


@Slf4j
@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired JPAQueryFactory queryFactory;

    @BeforeEach
    public void createMember() {
        Member member = Member.builder()
                .email("sodev@gmail.com")
                .password("1234")
                .nickName("sodev")
                .introduce("test introduce")
                .build();
        memberRepository.save(member);
    }

    @Test
    public void test() throws Exception {
        Member findMember = queryFactory.selectFrom(member)
                .where(member.id.eq(1L))
                .fetchOne();

        log.info("member={}", findMember);
        log.info("member.email={}", findMember.getEmail());
    }

}