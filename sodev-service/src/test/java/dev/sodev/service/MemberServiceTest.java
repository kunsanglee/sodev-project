package dev.sodev.service;

import dev.sodev.config.AbstractServiceTest;
import dev.sodev.domain.entity.Member;
import dev.sodev.helper.MemberTestHelper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class MemberServiceTest extends AbstractServiceTest {

    @Test
    public void test() throws Exception {
        // given
        Member savedMember = memberRepository.save(MemberTestHelper.createMember());
        // when
        Member findMember = memberRepository.findById(1L).orElseThrow();
        // then

        log.info("findMember.userName = {}", findMember.getUserName());
        Assertions.assertThat(findMember).isEqualTo(savedMember);
        Assertions.assertThat(findMember.getUserName()).isEqualTo("testUser");
    }

}
