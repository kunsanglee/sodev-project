package dev.be.sodevcommon.repository;

import dev.be.sodevcommon.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
