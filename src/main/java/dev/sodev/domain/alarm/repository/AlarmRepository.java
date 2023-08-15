package dev.sodev.domain.alarm.repository;

import dev.sodev.domain.alarm.Alarm;
import dev.sodev.domain.member.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AlarmRepository extends JpaRepository<Alarm, Long>, AlarmCustomRepository {

    Slice<Alarm> findAllByMember(Member member, Pageable pageable);

    @Modifying
    @Query("delete from Alarm a where a.member.id = :memberId")
    void deleteAllByMemberId(Long memberId);
}
