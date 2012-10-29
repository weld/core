package org.jboss.weld.annotated.slim.backed;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Set;

import org.jboss.weld.annotated.slim.BaseAnnotated;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.ReflectionCache;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.LazyValueHolder;

import com.google.common.collect.ImmutableSet;

public abstract class BackedAnnotated extends BaseAnnotated {

    private final LazyValueHolder<Set<Type>> typeClosure;

    public BackedAnnotated(Type baseType, ClassTransformer transformer) {
        super(baseType);
        this.typeClosure = initTypeClosure(baseType, transformer.getSharedObjectCache());
    }

    protected LazyValueHolder<Set<Type>> initTypeClosure(Type baseType, SharedObjectCache cache) {
        return cache.getTypeClosureHolder(baseType);
    }

    public Set<Type> getTypeClosure() {
        return typeClosure.get();
    }

    protected abstract AnnotatedElement getAnnotatedElement();

    protected abstract ReflectionCache getReflectionCache();

    @Override
    public Set<Annotation> getAnnotations() {
        return ImmutableSet.copyOf(getReflectionCache().getAnnotations(getAnnotatedElement()));
    }
}
