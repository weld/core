package org.jboss.weld.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

/**
 * An {@link InjectionPoint} implementation for programmatic/synthetic contexts where there is
 * no source-level element (no {@link Annotated}, no {@link Member}) but the owning {@link Bean}
 * is known. This is used for injection points created by {@code BeanConfigurator.produceWith()}
 * callbacks and for deserialization of such injection points.
 *
 * <p>
 * In contrast with {@link EmptyInjectionPoint}, which returns {@code null} for all metadata
 * including the bean, this class always carries a non-null bean reference.
 */
public class SyntheticInjectionPoint implements InjectionPoint {

    private final Bean<?> bean;
    private final Type type;
    private final Set<Annotation> qualifiers;

    public SyntheticInjectionPoint(Bean<?> bean, Type type, Set<Annotation> qualifiers) {
        this.bean = bean;
        this.type = type;
        this.qualifiers = qualifiers;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Bean<?> getBean() {
        return bean;
    }

    @Override
    public Member getMember() {
        return null;
    }

    @Override
    public Annotated getAnnotated() {
        return null;
    }

    @Override
    public boolean isDelegate() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return false;
    }
}
