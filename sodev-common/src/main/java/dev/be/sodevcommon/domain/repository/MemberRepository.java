package dev.be.sodevcommon.domain.repository;

import dev.be.sodevcommon.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
