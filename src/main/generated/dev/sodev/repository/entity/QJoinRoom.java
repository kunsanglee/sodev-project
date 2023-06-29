package dev.sodev.repository.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJoinRoom is a Querydsl query type for JoinRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJoinRoom extends EntityPathBase<JoinRoom> {

    private static final long serialVersionUID = 849751514L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJoinRoom joinRoom = new QJoinRoom("joinRoom");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final QChat chat;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> joinDate = createDateTime("joinDate", java.time.LocalDateTime.class);

    public final QMember member;

    public final QMessage message;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    //inherited
    public final StringPath modifiedBy = _super.modifiedBy;

    public QJoinRoom(String variable) {
        this(JoinRoom.class, forVariable(variable), INITS);
    }

    public QJoinRoom(Path<? extends JoinRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJoinRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJoinRoom(PathMetadata metadata, PathInits inits) {
        this(JoinRoom.class, metadata, inits);
    }

    public QJoinRoom(Class<? extends JoinRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chat = inits.isInitialized("chat") ? new QChat(forProperty("chat"), inits.get("chat")) : null;
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member"), inits.get("member")) : null;
        this.message = inits.isInitialized("message") ? new QMessage(forProperty("message"), inits.get("message")) : null;
    }

}

