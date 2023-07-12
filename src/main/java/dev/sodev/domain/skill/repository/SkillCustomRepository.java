package dev.sodev.domain.skill.repository;

import dev.sodev.domain.skill.Skill;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface SkillCustomRepository {
    Optional<Skill> findAndUpdate(String skillName);
    void usagePlus(List<Skill> skills);
}
