package dev.be.sodevcommon.repository;

import dev.be.sodevcommon.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
