package dev.sodev.domain.Images;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QImages is a Querydsl query type for Images
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QImages extends BeanPath<Images> {

    private static final long serialVersionUID = -893145326L;

    public static final QImages images = new QImages("images");

    public final StringPath imageName = createString("imageName");

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath imageUuid = createString("imageUuid");

    public QImages(String variable) {
        super(Images.class, forVariable(variable));
    }

    public QImages(Path<? extends Images> path) {
        super(path.getType(), path.getMetadata());
    }

    public QImages(PathMetadata metadata) {
        super(Images.class, metadata);
    }

}

