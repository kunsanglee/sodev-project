package dev.sodev.domain.skill.repository;


import dev.sodev.domain.skill.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface SkillRepository extends JpaRepository<Skill, Long>, SkillCustomRepository{

    Optional<Skill> findSkillByName(String name);

}
