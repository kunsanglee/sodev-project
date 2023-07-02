package dev.sodev.fixture;

import dev.sodev.domain.entity.Member;
import dev.sodev.domain.enums.Auth;

public class MemberFixture {

    public static Member get(String email, String password) {
        return Member.builder()
                .id(1L)
                .email(email)
                .password(password)
                .auth(Auth.MEMBER)
                .nickName("test")
                .build();
    }
}
