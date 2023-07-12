package dev.sodev.domain.message;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMessageText is a Querydsl query type for MessageText
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMessageText extends EntityPathBase<MessageText> {

    private static final long serialVersionUID = -782683215L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMessageText messageText = new QMessageText("messageText");

    public final QMessage _super;

    // inherited
    public final dev.sodev.domain.chat.QChat chat;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt;

    //inherited
    public final StringPath createdBy;

    //inherited
    public final NumberPath<Long> id;

    // inherited
    public final dev.sodev.domain.member.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt;

    //inherited
    public final StringPath modifiedBy;

    public final StringPath text = createString("text");

    public QMessageText(String variable) {
        this(MessageText.class, forVariable(variable), INITS);
    }

    public QMessageText(Path<? extends MessageText> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMessageText(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMessageText(PathMetadata metadata, PathInits inits) {
        this(MessageText.class, metadata, inits);
    }

    public QMessageText(Class<? extends MessageText> type, PathMetadata metadata, PathInits inits) {
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

