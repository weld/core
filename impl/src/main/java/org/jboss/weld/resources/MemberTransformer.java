/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.resources;

import static org.jboss.weld.logging.messages.BeanMessage.INVALID_ANNOTATED_CALLABLE;
import static org.jboss.weld.logging.messages.BeanMessage.INVALID_ANNOTATED_MEMBER;
import static org.jboss.weld.logging.messages.BeanMessage.UNABLE_TO_LOAD_MEMBER;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMember;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * Transforms a given {@link AnnotatedMember} into its richer counterpart {@link WeldMember}.
 *
 * @author Jozef Hartinger
 *
 */
public class MemberTransformer implements Service {

    private final ConcurrentMap<AnnotatedMember<?>, WeldMember<?, ?, ?>> memberCache;
    private final ClassTransformer transformer;
    private final FieldLoader fieldLoader;
    private final MethodLoader methodLoader;
    private final ConstructorLoader constructorLoader;

    public MemberTransformer(ClassTransformer transformer) {
        this.transformer = transformer;
        this.fieldLoader = new FieldLoader();
        this.methodLoader = new MethodLoader();
        this.constructorLoader = new ConstructorLoader();
        this.memberCache = new MapMaker().makeComputingMap(new TransformationFunction());
    }

    public <T, X> WeldField<T, X> load(AnnotatedField<X> field) {
        if (field instanceof WeldField<?, ?>) {
            return Reflections.cast(field);
        }
        return Reflections.cast(memberCache.get(field));
    }

    public <T, X> WeldMethod<T, X> load(AnnotatedMethod<X> method) {
        if (method instanceof WeldMethod<?, ?>) {
            return Reflections.cast(method);
        }
        return Reflections.cast(memberCache.get(method));
    }

    public <T> WeldConstructor<T> load(AnnotatedConstructor<T> constructor) {
        if (constructor instanceof WeldConstructor<?>) {
            return Reflections.cast(constructor);
        }
        return Reflections.cast(memberCache.get(constructor));
    }

    public <T, X> WeldParameter<T, X> load(AnnotatedParameter<X> parameter) {
        if (parameter instanceof WeldParameter<?, ?>) {
            return Reflections.cast(parameter);
        }
        if (parameter.getDeclaringCallable() instanceof AnnotatedMethod<?>) {
            return Reflections.cast(load((AnnotatedMethod<?>) parameter.getDeclaringCallable()).getWeldParameters().get(parameter.getPosition()));
        } else if (parameter.getDeclaringCallable() instanceof AnnotatedConstructor<?>) {
            return Reflections.cast(load((AnnotatedConstructor<?>) parameter.getDeclaringCallable()).getWeldParameters().get(parameter.getPosition()));
        } else {
            throw new IllegalArgumentException(INVALID_ANNOTATED_CALLABLE, parameter.getDeclaringCallable());
        }
    }

    private class TransformationFunction implements Function<AnnotatedMember<?>, WeldMember<?, ?, ?>> {
        @Override
        public WeldMember<?, ?, ?> apply(AnnotatedMember<?> from) {
            if (from instanceof AnnotatedField<?>) {
                return fieldLoader.load(Reflections.<AnnotatedField<?>> cast(from));
            }
            if (from instanceof AnnotatedMethod<?>) {
                return methodLoader.load(Reflections.<AnnotatedMethod<?>> cast(from));
            }
            if (from instanceof AnnotatedConstructor<?>) {
                return constructorLoader.load(Reflections.<AnnotatedConstructor<?>> cast(from));
            }
            throw new IllegalArgumentException(INVALID_ANNOTATED_MEMBER, from);
        }
    }

    private abstract class AbstractMemberLoader<A extends AnnotatedMember<?>, W extends WeldMember<?, ?, ?>> {

        public W load(A source) {
            return findMatching(getMembersOfDeclaringType(source), source);
        }

        public W findMatching(Collection<W> members, A source) {
            for (W member : members) {
                if (equals(member, source)) {
                    return member;
                }
            }
            throw new IllegalStateException(UNABLE_TO_LOAD_MEMBER, source);
        }

        public abstract boolean equals(W member1, A member2);

        public abstract Collection<W> getMembersOfDeclaringType(A source);
    }

    private class FieldLoader extends AbstractMemberLoader<AnnotatedField<?>, WeldField<?, ?>> {
        @Override
        public boolean equals(WeldField<?, ?> member1, AnnotatedField<?> member2) {
            return AnnotatedTypes.compareAnnotatedField(member1, member2);
        }

        @Override
        public Collection<WeldField<?, ?>> getMembersOfDeclaringType(AnnotatedField<?> source) {
            return Reflections.cast(transformer.loadClass(source.getDeclaringType()).getDeclaredWeldFields());
        }
    }

    private class MethodLoader extends AbstractMemberLoader<AnnotatedMethod<?>, WeldMethod<?, ?>> {
        @Override
        public boolean equals(WeldMethod<?, ?> member1, AnnotatedMethod<?> member2) {
            return AnnotatedTypes.compareAnnotatedCallable(member1, member2);
        }

        @Override
        public Collection<WeldMethod<?, ?>> getMembersOfDeclaringType(AnnotatedMethod<?> source) {
            return Reflections.cast(transformer.loadClass(source.getDeclaringType()).getDeclaredWeldMethods());
        }
    }

    private class ConstructorLoader extends AbstractMemberLoader<AnnotatedConstructor<?>, WeldConstructor<?>> {
        @Override
        public boolean equals(WeldConstructor<?> member1, AnnotatedConstructor<?> member2) {
            return AnnotatedTypes.compareAnnotatedCallable(member1, member2);
        }

        @Override
        public Collection<WeldConstructor<?>> getMembersOfDeclaringType(AnnotatedConstructor<?> source) {
            return Reflections.cast(transformer.loadClass(source.getDeclaringType()).getWeldConstructors());
        }
    }

    @Override
    public void cleanup() {
        memberCache.clear();
    }
}
