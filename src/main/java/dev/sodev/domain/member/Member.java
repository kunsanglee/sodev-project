package dev.sodev.domain.member;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.Images.Images;
import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.enums.Auth;
import dev.sodev.domain.follow.Follow;
import dev.sodev.domain.likes.Likes;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
    @Builder.Default
    private String introduce = "asd";

    @Builder.Default
    @OneToMany(mappedBy = "toMember", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Follow> followers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "fromMember", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Follow> following = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Where(clause = "role = 'CREATOR' or role = 'MEMBER'")
    private List<MemberProject> memberProject = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Where(clause = "role = 'APPLICANT'")
    private List<MemberProject> applies = new ArrayList<>(); // 회원의 지원 프로젝트 리스트

    @Builder.Default
    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    @Embedded
    private Images images;

//    private LocalDateTime removedAt;


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

    public void removeAllComments() {
        for (Comment comment : comments) {
//            comment.remove(); // 댓글을 논리적으로 삭제
            comment.updateMember(null); // 회원과 댓글 연관관계 제거
        }
    }

}
