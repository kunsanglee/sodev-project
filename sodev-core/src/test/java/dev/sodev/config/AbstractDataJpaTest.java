package dev.sodev.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestConfig.class)
public abstract class AbstractDataJpaTest {

    @Autowired protected EntityManager em;
    @Autowired protected JPAQueryFactory queryFactory;
}
