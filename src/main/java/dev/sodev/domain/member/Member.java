package dev.sodev.domain.member;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.Images.Images;
import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.enums.Auth;
import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.follow.Follow;
import dev.sodev.domain.follow.dto.FollowRequest;
import dev.sodev.domain.likes.Likes;
import dev.sodev.domain.member.dto.request.MemberUpdateRequest;
import dev.sodev.domain.project.Project;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
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
    private String introduce;

    @Builder.Default
    @OneToMany(mappedBy = "toMember")
    private List<Follow> followers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "fromMember")
    private List<Follow> following = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member")
    @Where(clause = "role = 'CREATOR' or role = 'MEMBER'")
    private List<MemberProject> memberProject = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member")
    @Where(clause = "role = 'APPLICANT'")
    private List<MemberProject> applies = new ArrayList<>(); // 회원의 지원 프로젝트 리스트

    @Builder.Default
    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member")
    private List<Likes> likes = new ArrayList<>();

    @Embedded
    private Images images;


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
        comments.forEach(comment -> comment.updateMember(null)); // 회원과 댓글 연관관계 제거
        comments.clear();
    }

    public void updateMemberInfo(MemberUpdateRequest request) {
        this.updateNickName(request.nickName());
        this.updatePhone(request.phone());
        this.updateIntroduce(request.introduce());
        this.updateImage(request.memberImage());
    }

    // 회원이 이미 진행중이거나, 참여중인 프로젝트가 있는지 확인.
    public void isAlreadyInProject() {
        this.getMemberProject().stream()
                .map(MemberProject::getProjectRole)
                .filter(pr -> pr.getRole().equals(ProjectRole.Role.CREATOR) || pr.getRole().equals(ProjectRole.Role.MEMBER))
                .findAny()
                .ifPresent(state -> {
                    throw new SodevApplicationException(ErrorCode.ALREADY_IN_PROJECT);
                });
    }

    // 작성자와 요청자가 다를경우 에러반환.
    public void isCreator(Project project) {
        if (!project.getCreatedBy().equals(this.getEmail())) throw new SodevApplicationException(ErrorCode.INVALID_PERMISSION);
    }

    public List<Member> alarmsToFollower() {
        return this.getFollowers().stream().map(Follow::getFromMember).toList();
    }

    // 본인을 팔로우 하려는지 확인
    public void isOtherMember(FollowRequest request, String message) {
        if (request.toId().equals(this.getId())) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, message);
        }
    }

    public Follow follow(Member targetMember) {
        Follow follow = Follow.getFollow(this, targetMember);
        this.getFollowing().add(follow);
        targetMember.getFollowers().add(follow);
        return follow;
    }

    public Follow unfollow(Member targetMember) {
        Follow follow = this.getFollowing().stream()
                .filter(f -> f.getToMember().equals(targetMember))
                .findAny()
                .orElseThrow(() -> new SodevApplicationException(ErrorCode.FOLLOW_NOT_FOUND));
        this.getFollowing().remove(follow);
        targetMember.followers.remove(follow);
        return follow;
    }
}
