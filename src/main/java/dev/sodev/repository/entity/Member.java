package dev.sodev.repository.entity;

import dev.sodev.repository.enums.Auth;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;
    private String email;
    private String pwd;

    @Enumerated(EnumType.STRING)
    private Auth auth;

    private String nickName;
    private String phone;
    private String introduce;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_project_id")
    private MemberProject memberProject;

    private String imageUrl;
    private String imageUuid;
    private String imageName;


}
