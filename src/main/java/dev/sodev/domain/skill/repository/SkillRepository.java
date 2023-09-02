package dev.sodev.domain.skill.repository;


import dev.sodev.domain.skill.Skill;
import dev.sodev.domain.skill.repository.query.SkillCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface SkillRepository extends JpaRepository<Skill, Long>, SkillCustomRepository {

    Optional<Skill> findSkillByName(String name);

}
