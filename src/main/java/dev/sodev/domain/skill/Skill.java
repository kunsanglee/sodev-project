package dev.sodev.domain.skill;

import dev.sodev.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Skill extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Long id;
    private String name;
    private Long usage;

    public static Skill of(String name) {
       return new Skill(null,name,0L);
    }
}
