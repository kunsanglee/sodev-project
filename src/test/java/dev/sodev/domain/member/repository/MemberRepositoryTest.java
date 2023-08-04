package dev.sodev.domain.member.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.member.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static dev.sodev.domain.member.QMember.member;


@Slf4j
@SpringBootTest
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired JPAQueryFactory queryFactory;

    @BeforeEach
    public void createMember() {
        SecurityContextHolder.getContext().setAuthentication(getAuthenticatedUser());
        Member member = Member.builder()
                .email("sodev@gmail.com")
                .password("1234")
                .nickName("sodev")
                .introduce("test introduce")
                .build();
        memberRepository.save(member);
    }

    private UsernamePasswordAuthenticationToken getAuthenticatedUser() {
        User user = new User("username", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @WithMockUser
    @Test
    public void test() throws Exception {
        Member findMember = queryFactory.selectFrom(member)
                .where(member.id.eq(1L))
                .fetchOne();

        log.info("member={}", findMember);
        log.info("member.email={}", findMember.getEmail());
    }

}