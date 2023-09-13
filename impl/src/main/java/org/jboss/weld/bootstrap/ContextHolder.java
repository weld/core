package org.jboss.weld.bootstrap;

import java.lang.annotation.Annotation;
import java.util.Set;

import jakarta.enterprise.context.spi.Context;

public class ContextHolder<T extends Context> {

    private final T context;
    private final Class<T> type;
    private final Set<Annotation> qualifiers;

    public ContextHolder(T context, Class<T> type, Set<Annotation> qualifiers) {
        this.context = context;
        this.type = type;
        this.qualifiers = qualifiers;
    }

    public T getContext() {
        return context;
    }

    public Class<T> getType() {
        return type;
    }

    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

}
