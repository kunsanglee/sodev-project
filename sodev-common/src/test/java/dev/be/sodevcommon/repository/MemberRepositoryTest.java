package dev.be.sodevcommon.repository;


import dev.be.sodevcommon.config.AbstractDataJpaTest;
import dev.be.sodevcommon.domain.entity.Member;
import dev.be.sodevcommon.domain.repository.MemberRepository;
import dev.be.sodevcommon.helper.MemberTestHelper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static dev.be.sodevcommon.domain.entity.QMember.*;

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