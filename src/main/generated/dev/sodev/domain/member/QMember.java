package dev.sodev.domain.member;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -344754958L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMember member = new QMember("member1");

    public final dev.sodev.domain.QBaseEntity _super = new dev.sodev.domain.QBaseEntity(this);

    public final EnumPath<dev.sodev.domain.enums.Auth> auth = createEnum("auth", dev.sodev.domain.enums.Auth.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final StringPath email = createString("email");

    public final NumberPath<Long> follower = createNumber("follower", Long.class);

    public final NumberPath<Long> following = createNumber("following", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final dev.sodev.domain.Images.QImages images;

    public final StringPath introduce = createString("introduce");

    public final QMemberProject memberProject;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    //inherited
    public final StringPath modifiedBy = _super.modifiedBy;

    public final StringPath nickName = createString("nickName");

    public final StringPath password = createString("password");

    public final StringPath phone = createString("phone");

    public final StringPath refreshToken = createString("refreshToken");

    public final DateTimePath<java.time.LocalDateTime> removedAt = createDateTime("removedAt", java.time.LocalDateTime.class);

    public QMember(String variable) {
        this(Member.class, forVariable(variable), INITS);
    }

    public QMember(Path<? extends Member> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMember(PathMetadata metadata, PathInits inits) {
        this(Member.class, metadata, inits);
    }

    public QMember(Class<? extends Member> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.images = inits.isInitialized("images") ? new dev.sodev.domain.Images.QImages(forProperty("images")) : null;
        this.memberProject = inits.isInitialized("memberProject") ? new QMemberProject(forProperty("memberProject"), inits.get("memberProject")) : null;
    }

}

