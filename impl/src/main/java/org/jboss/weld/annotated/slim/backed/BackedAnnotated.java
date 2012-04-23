package org.jboss.weld.annotated.slim.backed;

import java.lang.reflect.Type;
import java.util.Set;

import org.jboss.weld.annotated.slim.BaseAnnotated;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.LazyValueHolder;

public abstract class BackedAnnotated extends BaseAnnotated {

    private final LazyValueHolder<Set<Type>> typeClosure;

    public BackedAnnotated(Type baseType, SharedObjectCache cache) {
        super(baseType);
        this.typeClosure = cache.getTypeClosureHolder(baseType);
    }

    public Set<Type> getTypeClosure() {
        return typeClosure.get();
    }

}
