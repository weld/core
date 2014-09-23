package org.jboss.weld.annotated.slim;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.Annotated;

import org.jboss.weld.experimental.ExperimentalAnnotated;

public abstract class BaseAnnotated implements Annotated, ExperimentalAnnotated {

    private final Type baseType;

    public BaseAnnotated(Type baseType) {
        this.baseType = baseType;
    }

    public Type getBaseType() {
        return baseType;
    }

}
