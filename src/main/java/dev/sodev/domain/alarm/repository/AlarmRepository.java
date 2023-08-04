package dev.sodev.domain.alarm.repository;

import dev.sodev.domain.alarm.Alarm;
import dev.sodev.domain.member.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long>, AlarmCustomRepository {

    Slice<Alarm> findAllByMember(Member member, Pageable pageable);
}
