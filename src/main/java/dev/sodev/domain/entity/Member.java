package dev.sodev.domain.entity;

import dev.sodev.domain.enums.Auth;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE \"member\" SET removed_at = NOW() WHERE id=?")
@Where(clause = "removed_at is NULL")
@Entity
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;
    private String email;
    private String pwd;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Auth auth = Auth.MEMBER;

    private String nickName;
    private String phone;
    private String introduce;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_project_id")
    private MemberProject memberProject;

    private String imageUrl;
    private String imageUuid;
    private String imageName;

    private LocalDateTime removedAt;


}
