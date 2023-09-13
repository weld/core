package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.WildcardType;

import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;

class WildcardTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedWildcardType> implements WildcardType {
    private final boolean hasUpperBound;

    // note that while java.lang.reflect.AnnotatedWildcardType API returns arrays,
    // the Java language only permits at most one upper or lower bound

    WildcardTypeImpl(java.lang.reflect.AnnotatedWildcardType reflectionType, BeanManager bm) {
        this(reflectionType, null, bm);
    }

    WildcardTypeImpl(java.lang.reflect.AnnotatedWildcardType reflectionType, AnnotationOverrides overrides,
            BeanManager bm) {
        super(reflectionType, overrides, bm);
        this.hasUpperBound = reflectionType.getAnnotatedLowerBounds().length == 0;
    }

    @Override
    public Type upperBound() {
        if (!hasUpperBound) {
            return null;
        }

        return TypeImpl.fromReflectionType(reflection.getAnnotatedUpperBounds()[0], bm);
    }

    @Override
    public Type lowerBound() {
        if (hasUpperBound) {
            return null;
        }

        return TypeImpl.fromReflectionType(reflection.getAnnotatedLowerBounds()[0], bm);
    }
}
