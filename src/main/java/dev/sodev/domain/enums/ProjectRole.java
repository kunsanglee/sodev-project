package dev.sodev.domain.enums;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public static ProjectRole setProjectRole(ProjectRole.Role role, ProjectRole.RoleType roleType) {
        return ProjectRole.builder()
                .role(role)
                .roleType(roleType)
                .build();
    }
}
