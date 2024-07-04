package com.chimaera.wagubook.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLocation is a Querydsl query type for Location
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QLocation extends BeanPath<Location> {

    private static final long serialVersionUID = -2137001704L;

    public static final QLocation location = new QLocation("location");

    public final StringPath address = createString("address");

    public final NumberPath<Double> posx = createNumber("posx", Double.class);

    public final NumberPath<Double> posy = createNumber("posy", Double.class);

    public QLocation(String variable) {
        super(Location.class, forVariable(variable));
    }

    public QLocation(Path<? extends Location> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLocation(PathMetadata metadata) {
        super(Location.class, metadata);
    }

}

