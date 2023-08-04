package dev.sodev.domain.enums;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRole {

    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    public enum Role {
        CREATOR,
        MEMBER,
        APPLICANT
    }

    public enum RoleType {
        FE,
        BE
    }

    public static ProjectRole creatorOf(ProjectRole.RoleType roleType) {
        return ProjectRole.builder()
                .role(Role.CREATOR)
                .roleType(roleType)
                .build();
    }

    public static ProjectRole memberOf(ProjectRole.RoleType roleType) {
        return ProjectRole.builder()
                .role(Role.MEMBER)
                .roleType(roleType)
                .build();
    }

    public static ProjectRole applicantOf(ProjectRole.RoleType roleType) {
        return ProjectRole.builder()
                .role(Role.APPLICANT)
                .roleType(roleType)
                .build();
    }
}
