package dev.sodev.domain.repository;


import dev.sodev.config.AbstractDataJpaTest;
import dev.sodev.domain.entity.Member;
import dev.sodev.domain.repository.MemberRepository;
import dev.sodev.helper.MemberTestHelper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static dev.sodev.domain.entity.QMember.*;

@Slf4j
class MemberRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void test() {
        List<Member> fetch = queryFactory.
                selectFrom(member)
                .fetch();
        log.info("fetch.size = {}",fetch.size());
        Assertions.assertThat(fetch.size()).isEqualTo(0);
    }

    @Test
    public void save() throws Exception {
        Member member = MemberTestHelper.createMember();
        memberRepository.save(member);

        Member findMember = memberRepository.findById(1L).orElseThrow();

        Assertions.assertThat(findMember).isEqualTo(member);
        Assertions.assertThat(findMember.getUserName()).isEqualTo("testUser");
    }

}