package dev.sodev.domain.follow;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFollow is a Querydsl query type for Follow
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFollow extends EntityPathBase<Follow> {

    private static final long serialVersionUID = 1869962514L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFollow follow = new QFollow("follow");

    public final dev.sodev.domain.QBaseEntity _super = new dev.sodev.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final dev.sodev.domain.member.QMember fromMember;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    //inherited
    public final StringPath modifiedBy = _super.modifiedBy;

    public final dev.sodev.domain.member.QMember toMember;

    public QFollow(String variable) {
        this(Follow.class, forVariable(variable), INITS);
    }

    public QFollow(Path<? extends Follow> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFollow(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFollow(PathMetadata metadata, PathInits inits) {
        this(Follow.class, metadata, inits);
    }

    public QFollow(Class<? extends Follow> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.fromMember = inits.isInitialized("fromMember") ? new dev.sodev.domain.member.QMember(forProperty("fromMember"), inits.get("fromMember")) : null;
        this.toMember = inits.isInitialized("toMember") ? new dev.sodev.domain.member.QMember(forProperty("toMember"), inits.get("toMember")) : null;
    }

}

