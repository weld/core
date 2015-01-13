package org.jboss.weld.injection;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

public class EmptyInjectionPoint implements InjectionPoint, Serializable {

    private static final long serialVersionUID = -2041468540191211977L;

    public static final InjectionPoint INSTANCE = new EmptyInjectionPoint();

    protected EmptyInjectionPoint() {
    }

    public Type getType() {
        return Object.class;
    }

    public Set<Annotation> getQualifiers() {
        return Collections.emptySet();
    }

    public Bean<?> getBean() {
        return null;
    }

    public Member getMember() {
        return null;
    }

    public Annotated getAnnotated() {
        return null;
    }

    public boolean isDelegate() {
        return false;
    }

    public boolean isTransient() {
        return false;
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}
