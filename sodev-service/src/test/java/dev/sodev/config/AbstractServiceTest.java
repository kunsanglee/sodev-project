package dev.sodev.config;

import dev.sodev.domain.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@Transactional
@SpringBootTest
public abstract class AbstractServiceTest {

    @Autowired
    protected EntityManager em;

    @Autowired
    protected MemberRepository memberRepository;
}
