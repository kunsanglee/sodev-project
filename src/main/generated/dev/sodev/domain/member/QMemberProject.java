package dev.sodev.domain.member;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberProject is a Querydsl query type for MemberProject
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberProject extends EntityPathBase<MemberProject> {

    private static final long serialVersionUID = 1764186567L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberProject memberProject = new QMemberProject("memberProject");

    public final dev.sodev.domain.QBaseEntity _super = new dev.sodev.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    //inherited
    public final StringPath modifiedBy = _super.modifiedBy;

    public final dev.sodev.domain.project.QProject project;

    public final EnumPath<dev.sodev.domain.enums.ProjectState> state = createEnum("state", dev.sodev.domain.enums.ProjectState.class);

    public QMemberProject(String variable) {
        this(MemberProject.class, forVariable(variable), INITS);
    }

    public QMemberProject(Path<? extends MemberProject> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberProject(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberProject(PathMetadata metadata, PathInits inits) {
        this(MemberProject.class, metadata, inits);
    }

    public QMemberProject(Class<? extends MemberProject> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member"), inits.get("member")) : null;
        this.project = inits.isInitialized("project") ? new dev.sodev.domain.project.QProject(forProperty("project")) : null;
    }

}

