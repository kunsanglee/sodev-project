package dev.sodev.helper;

import dev.sodev.domain.entity.Member;
import dev.sodev.domain.enums.Auth;
import dev.sodev.domain.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberTestHelper {

    @Autowired
    MemberRepository memberRepository;

    public static final String userName = "testUser";
    public static final String pwd = "password";
    public static final String nickName = "testNickname";
    public static final String phone = "010-1234-1234";
    public static final String introduce = "테스트 소개입니다.";
    public static final Auth auth = Auth.MEMBER;


    public static Member createMember() {
        return Member.builder()
                .userName(userName)
                .pwd(pwd)
                .auth(auth)
                .nickName(nickName)
                .phone(phone)
                .introduce(introduce)
                .build();
    }

}
