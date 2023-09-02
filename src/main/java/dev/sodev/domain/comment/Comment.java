package dev.sodev.domain.comment;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.project.Project;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "comment", indexes = {
        @Index(name = "idx_comment_member", columnList = "member_id"),
        @Index(name = "idx_comment_project", columnList = "project_id")
})
@Entity
public class Comment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", updatable = false)
    private long id;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "project_id", updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @Column(nullable = false)
    private String content;

    @JoinColumn(name = "parent_id", updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Comment parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Builder.Default
    private boolean isRemoved = false;


    public void confirmWriter(Member member) {
        this.member = member;
        member.getComments().add(this);
    }

    public void confirmProject(Project project) {
        this.project = project;
        project.getComments().add(this);
    }

    public void confirmParent(Comment parent){
        this.parent = parent;
        parent.addChild(this);
    }

    public void addChild(Comment child){
        children.add(child);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateMember(Member member) {
        this.member = member;
    }

    public void remove() {
        this.isRemoved = true;
    }

    public Comment isRemovedAndWriter(Member member) {
        if (this.isRemoved()) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "이미 삭제된 댓글입니다");
        } else if (!this.getMember().getId().equals(member.getId())) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "댓글 작성자가 일치하지 않습니다.");
        }
        return this;
    }

    // 삭제해도 되는 댓글리스트 반환(자식 댓글이 전부 isRemoved = true)
    public List<Comment> findRemovableList() {

        List<Comment> result = new ArrayList<>();

        Optional.ofNullable(this.parent).ifPresentOrElse(
                parentComment ->{ // 대댓글인 경우 (부모가 존재하는 경우)
                    if( parentComment.isRemoved() && parentComment.isAllChildRemoved()){
                        result.addAll(parentComment.getChildren());
                        result.add(parentComment);
                    }
                },
                () -> { // 댓글인 경우
                    if (isAllChildRemoved()) {
                        result.add(this);
                        result.addAll(this.getChildren());
                    }
                }
        );

        return result;
    }

    // 모든 자식 댓글이 삭제되었는지 판단,
    private boolean isAllChildRemoved() {
        return getChildren().stream()
                .map(Comment::isRemoved) // 지워졌는지 여부로 바꾼다
                .filter(isRemove -> !isRemove) // 지워졌으면 true, 안지워졌으면 false. filter에 걸러지는 것은 false, 있다면 false 없다면 true.
                .findAny() // 지워지지 않은게 하나라도 있다면 false를 반환
                .orElse(true); // 모두 지워졌다면 true를 반환

    }


}
