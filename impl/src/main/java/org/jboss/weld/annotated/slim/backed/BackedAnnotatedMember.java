package org.jboss.weld.annotated.slim.backed;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import jakarta.enterprise.inject.spi.AnnotatedMember;

import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.ReflectionCache;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.serialization.AbstractSerializableHolder;
import org.jboss.weld.util.reflection.Reflections;

public abstract class BackedAnnotatedMember<X> extends BackedAnnotated implements AnnotatedMember<X> {

    private BackedAnnotatedType<X> declaringType;

    public BackedAnnotatedMember(Type baseType, BackedAnnotatedType<X> declaringType, SharedObjectCache sharedObjectCache) {
        super(baseType, sharedObjectCache);
        this.declaringType = declaringType;
    }

    public boolean isStatic() {
        return Reflections.isStatic(getJavaMember());
    }

    public BackedAnnotatedType<X> getDeclaringType() {
        return declaringType;
    }

    @Override
    protected ReflectionCache getReflectionCache() {
        return getDeclaringType().getReflectionCache();
    }

    protected abstract static class BackedAnnotatedMemberSerializationProxy<X, A extends AnnotatedMember<? super X>>
            implements Serializable {

        private static final long serialVersionUID = 450947485748828056L;
        protected final BackedAnnotatedType<X> type;
        private final AbstractSerializableHolder<? extends Member> memberHolder;

        public BackedAnnotatedMemberSerializationProxy(BackedAnnotatedType<X> type,
                AbstractSerializableHolder<? extends Member> memberHolder) {
            this.type = type;
            this.memberHolder = memberHolder;
        }

        protected A resolve() {
            for (A annotatedMember : getCandidates()) {
                if (annotatedMember.getJavaMember().equals(memberHolder.get())) {
                    return annotatedMember;
                }
            }
            throw BeanLogger.LOG.unableToLoadMember(memberHolder.get());
        }

        protected abstract Iterable<A> getCandidates();
    }
}
