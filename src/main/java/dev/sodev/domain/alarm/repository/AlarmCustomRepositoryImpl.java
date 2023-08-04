package dev.sodev.domain.alarm.repository;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.alarm.Alarm;
import dev.sodev.domain.alarm.AlarmArgs;
import dev.sodev.domain.enums.AlarmType;
import dev.sodev.domain.member.Member;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

public class AlarmCustomRepositoryImpl implements AlarmCustomRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;


    public AlarmCustomRepositoryImpl(EntityManager em, JdbcTemplate jdbcTemplate) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void bulkAlarmsSave(List<Member> members, AlarmType alarmType, AlarmArgs args) {

        try {
            for (int i = 0; i < members.size(); i++) {
                Member member = members.get(i);

                // 각 멤버를 대상으로 Alarm 엔티티 생성
                Alarm alarm = Alarm.of(member, alarmType, args);

                // 생성된 Alarm 엔티티를 저장
                em.persist(alarm);

                // 일정 단위로 플러시와 클리어를 호출하여 메모리 사용량을 최적화
                if (i % 50 == 0) {
                    em.flush();
                    em.clear();
                }
            }
        } catch (Exception e) {
            // 예외 발생시 롤백이 자동으로 수행되며, 여기에는 오류 처리 로직이 들어갑니다.
            e.printStackTrace();
            throw new SodevApplicationException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
