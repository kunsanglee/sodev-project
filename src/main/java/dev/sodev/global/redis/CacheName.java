package dev.sodev.global.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheName {
    public static final String EMAIL = "member_email";
    public static final String MEMBER = "member_login";
    public static final String INFO = "member_info";
}
