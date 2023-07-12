package dev.sodev.domain.skill.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.skill.QSkill;
import dev.sodev.domain.skill.Skill;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

import static dev.sodev.domain.skill.QSkill.skill;

@Repository
@RequiredArgsConstructor
public class SkillCustomRepositoryImpl implements SkillCustomRepository{


    private final JPAQueryFactory queryFactory;




    @Override
    public Optional<Skill> findAndUpdate(String skillName) {

        return Optional.empty();
    }

    @Override
    public void usagePlus(List<Skill> skills) {

        queryFactory.update(skill)
                .set(skill.usage, skill.usage.add(1))
                .where(skill.in(skills))
                .execute();

    }


}
