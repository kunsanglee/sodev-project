package dev.sodev.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Auth {
    ADMIN("ROLE_ADMIN", "관리자"),
    MEMBER("ROLE_USER", "회원")
    ;

    private final String key;
    private final String title;
}
