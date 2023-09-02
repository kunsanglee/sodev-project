package dev.sodev.domain.enums;

import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
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

    // roleType 추출 메서드.
    public static ProjectRole.RoleType getRoleType(String type) {
        ProjectRole.RoleType role;
        if (type.equals("BE")) {
            role = ProjectRole.RoleType.BE;
        } else if (type.equals("FE")) {
            role = ProjectRole.RoleType.FE;
        } else {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "BE 또는 FE 둘 중 하나를 선택해주세요");
        }
        return role;
    }
}
