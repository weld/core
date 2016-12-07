package org.jboss.weld.annotated.slim;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.Annotated;

public abstract class BaseAnnotated implements Annotated {

    private final Type baseType;

    public BaseAnnotated(Type baseType) {
        this.baseType = baseType;
    }

    public Type getBaseType() {
        return baseType;
    }

}
