package dev.sodev.domain.member;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.Images.Images;
import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.enums.Auth;
import dev.sodev.domain.follow.Follow;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLDelete(sql = "UPDATE \"member\" SET removed_at = NOW() WHERE member_id=?")
@Where(clause = "removed_at is NULL")
@Entity
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true)
    private String email;
    private String password;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Auth auth = Auth.MEMBER;

    @Column(unique = true)
    private String nickName;
    private String phone;
    private String introduce;

    @Builder.Default
//    @OneToMany(mappedBy = "toMember", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OneToMany(mappedBy = "toMember")
    private List<Follow> followers = new ArrayList<>();

    @Builder.Default
//    @OneToMany(mappedBy = "fromMember", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OneToMany(mappedBy = "fromMember")
    private List<Follow> following = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_project_id")
    private MemberProject memberProject;

    @Builder.Default
//    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    @Embedded
    private Images images;

    private LocalDateTime removedAt;


    // 비밀번호 변경, 회원 탈퇴 시, 비밀번호를 확인하여 일치하는지 확인
    public boolean matchPassword(PasswordEncoder passwordEncoder, String checkPassword) {
        return passwordEncoder.matches(checkPassword, getPassword());
    }

    public void updatePassword(PasswordEncoder passwordEncoder, String password){
        this.password = passwordEncoder.encode(password);
    }

    public void updatePhone(String phone){
        this.phone = phone;
    }

    public void updateNickName(String nickName){
        this.nickName = nickName;
    }

    public void updateIntroduce(String introduce){
        this.introduce = introduce;
    }

    public void updateImage(Images memberImage) {
        this.images = memberImage;
    }

}
