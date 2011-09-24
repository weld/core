package org.jboss.weld.injection;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

public abstract class ForwardingInjectionPoint implements InjectionPoint {
    protected abstract InjectionPoint delegate();

    public Annotated getAnnotated() {
        return delegate().getAnnotated();
    }

    public Type getType() {
        return delegate().getType();
    }

    public Set<Annotation> getQualifiers() {
        return delegate().getQualifiers();
    }

    public Bean<?> getBean() {
        return delegate().getBean();
    }

    public Member getMember() {
        return delegate().getMember();
    }

    public boolean isDelegate() {
        return delegate().isDelegate();
    }

    public boolean isTransient() {
        return delegate().isTransient();
    }
}
