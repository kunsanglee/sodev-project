package dev.sodev.repository.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMessageImage is a Querydsl query type for MessageImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMessageImage extends EntityPathBase<MessageImage> {

    private static final long serialVersionUID = 139019209L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMessageImage messageImage = new QMessageImage("messageImage");

    public final QMessage _super;

    // inherited
    public final QChat chat;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt;

    //inherited
    public final StringPath createdBy;

    //inherited
    public final NumberPath<Long> id;

    // inherited
    public final QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt;

    //inherited
    public final StringPath modifiedBy;

    public final StringPath name = createString("name");

    public final StringPath url = createString("url");

    public final StringPath uuid = createString("uuid");

    public QMessageImage(String variable) {
        this(MessageImage.class, forVariable(variable), INITS);
    }

    public QMessageImage(Path<? extends MessageImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMessageImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMessageImage(PathMetadata metadata, PathInits inits) {
        this(MessageImage.class, metadata, inits);
    }

    public QMessageImage(Class<? extends MessageImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QMessage(type, metadata, inits);
        this.chat = _super.chat;
        this.createdAt = _super.createdAt;
        this.createdBy = _super.createdBy;
        this.id = _super.id;
        this.member = _super.member;
        this.modifiedAt = _super.modifiedAt;
        this.modifiedBy = _super.modifiedBy;
    }

}

