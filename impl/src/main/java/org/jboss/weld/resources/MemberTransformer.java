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

import static org.jboss.weld.logging.messages.BeanMessage.INVALID_ANNOTATED_MEMBER;
import static org.jboss.weld.logging.messages.BeanMessage.UNABLE_TO_LOAD_MEMBER;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedCallable;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMember;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedMember;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedMember;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedType;
import org.jboss.weld.annotated.slim.unbacked.UnbackedMemberIdentifier;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * Serves several functions. Firstly, transforms a given {@link AnnotatedMember} into its richer counterpart
 * {@link EnhancedAnnotatedMember}. Secondly, a {@link BackedAnnotatedMember} or {@link UnbackedAnnotatedMember} can be looked
 * up.
 *
 * @author Jozef Hartinger
 *
 */
public class MemberTransformer implements Service {

    private final ClassTransformer transformer;

    private final ConcurrentMap<Member, AnnotatedMember<?>> backedMemberCache;
    private final BackedFieldLoader backedFieldLoader;
    private final BackedMethodLoader backedMethodLoader;
    private final BackedConstructorLoader backedConstructorLoader;
    private final ConcurrentMap<UnbackedMemberIdentifier<?>, UnbackedAnnotatedMember<?>> unbackedAnnotatedMembersById;

    private final ConcurrentMap<AnnotatedMember<?>, EnhancedAnnotatedMember<?, ?, ?>> enhancedMemberCache;
    private final EnhancedFieldLoader enhancedFieldLoader;
    private final EnhancedMethodLoader enhancedMethodLoader;
    private final EnhancedConstructorLoader enhancedConstructorLoader;

    public MemberTransformer(ClassTransformer transformer) {
        this.transformer = transformer;
        this.backedFieldLoader = new BackedFieldLoader();
        this.backedMethodLoader = new BackedMethodLoader();
        this.backedConstructorLoader = new BackedConstructorLoader();
        this.backedMemberCache = new MapMaker().makeComputingMap(new BackedMemberLoaderFunction());
        this.unbackedAnnotatedMembersById = new MapMaker().makeComputingMap(new UnbackedMemberById());
        this.enhancedFieldLoader = new EnhancedFieldLoader();
        this.enhancedMethodLoader = new EnhancedMethodLoader();
        this.enhancedConstructorLoader = new EnhancedConstructorLoader();
        this.enhancedMemberCache = new MapMaker().makeComputingMap(new EnhancedMemberLoaderFunction());
    }

    // Backed members

    public AnnotatedMember<?> loadBackedMember(Member member) {
        return Reflections.cast(backedMemberCache.get(member));
    }

    private class BackedMemberLoaderFunction implements Function<Member, AnnotatedMember<?>> {
        @Override
        public AnnotatedMember<?> apply(Member member) {
            if (member instanceof Field) {
                return backedFieldLoader.load(Reflections.<Field> cast(member));
            }
            if (member instanceof Method) {
                return backedMethodLoader.load(Reflections.<Method> cast(member));
            }
            if (member instanceof Constructor<?>) {
                return backedConstructorLoader.load(Reflections.<Constructor<?>> cast(member));
            }
            throw new IllegalArgumentException(INVALID_ANNOTATED_MEMBER, member);
        }
    }

    /**
     * Iterates over members of a given {@link BackedAnnotatedType} to find a {@link BackedAnnotatedMember} implementation representing given member.
     *
     * @author Jozef Hartinger
     */
    private abstract class AbstractBackedMemberLoader<M extends Member, A extends AnnotatedMember<?>> {

        public A load(M member) {
            BackedAnnotatedType<?> type = transformer.getAnnotatedType(member.getDeclaringClass());
            return findMatching(getMembersOfDeclaringType(type), member);
        }

        public <X> A findMatching(Collection<? extends A> members, M member) {
            for (A annotatedMember : members) {
                if (member.equals(annotatedMember.getJavaMember())) {
                    return annotatedMember;
                }
            }
            throw new IllegalStateException(UNABLE_TO_LOAD_MEMBER, member);
        }

        public abstract Collection<? extends A> getMembersOfDeclaringType(BackedAnnotatedType<?> type);
    }

    private class BackedFieldLoader extends AbstractBackedMemberLoader<Field, AnnotatedField<?>> {
        @Override
        public Collection<? extends AnnotatedField<?>> getMembersOfDeclaringType(BackedAnnotatedType<?> type) {
            return type.getFields();
        }
    }

    private class BackedMethodLoader extends AbstractBackedMemberLoader<Method, AnnotatedMethod<?>> {
        @Override
        public Collection<? extends AnnotatedMethod<?>> getMembersOfDeclaringType(BackedAnnotatedType<?> type) {
            return type.getMethods();
        }
    }

    private class BackedConstructorLoader extends AbstractBackedMemberLoader<Constructor<?>, AnnotatedConstructor<?>> {
        @Override
        public Collection<? extends AnnotatedConstructor<?>> getMembersOfDeclaringType(BackedAnnotatedType<?> type) {
            return type.getConstructors();
        }
    }

    // Unbacked members

    public <X> UnbackedAnnotatedMember<X> getUnbackedMember(UnbackedMemberIdentifier<X> identifier) {
        return cast(unbackedAnnotatedMembersById.get(identifier));
    }

    /**
     * If an unbacked member is being deserialized it is looked in all the members of the declaring type and cached.
     */
    private class UnbackedMemberById implements Function<UnbackedMemberIdentifier<?>, UnbackedAnnotatedMember<?>> {

        @Override
        public UnbackedAnnotatedMember<?> apply(UnbackedMemberIdentifier<?> identifier) {
            return findMatchingMember(identifier.getType(), identifier.getMemberId());
        }

        private <T> UnbackedAnnotatedMember<T> findMatchingMember(UnbackedAnnotatedType<T> type, String id) {
            for (AnnotatedField<? super T> field : type.getFields()) {
                if (id.equals(AnnotatedTypes.createFieldId(field))) {
                    return cast(field);
                }
            }
            for (AnnotatedMethod<? super T> method : type.getMethods()) {
                if (id.equals(AnnotatedTypes.createMethodId(method.getJavaMember(), method.getAnnotations(), method.getParameters()))) {
                    return Reflections.cast(method);
                }
            }
            for (AnnotatedConstructor<T> constructor : type.getConstructors()) {
                if (id.equals(AnnotatedTypes.createConstructorId(constructor.getJavaMember(), constructor.getAnnotations(), constructor.getParameters()))) {
                    return cast(constructor);
                }
            }
            throw new WeldException(UNABLE_TO_LOAD_MEMBER, id);
        }
    }

    // Enhanced members

    public <X, A extends EnhancedAnnotatedMember<?, X, ? extends Member>> A loadEnhancedMember(AnnotatedMember<X> member) {
        if (member instanceof EnhancedAnnotatedMember<?, ?, ?>) {
            return Reflections.cast(member);
        }
        return Reflections.cast(enhancedMemberCache.get(member));
    }

    public <X> EnhancedAnnotatedParameter<?, X> load(AnnotatedParameter<X> parameter) {
        if (parameter instanceof EnhancedAnnotatedParameter<?, ?>) {
            return Reflections.cast(parameter);
        }
        EnhancedAnnotatedCallable<?, X, Member> callable = loadEnhancedMember(parameter.getDeclaringCallable());
        return callable.getEnhancedParameters().get(parameter.getPosition());
    }

    private class EnhancedMemberLoaderFunction implements Function<AnnotatedMember<?>, EnhancedAnnotatedMember<?, ?, ?>> {
        @Override
        public EnhancedAnnotatedMember<?, ?, ?> apply(AnnotatedMember<?> from) {
            if (from instanceof AnnotatedField<?>) {
                return enhancedFieldLoader.load(Reflections.<AnnotatedField<?>> cast(from));
            }
            if (from instanceof AnnotatedMethod<?>) {
                return enhancedMethodLoader.load(Reflections.<AnnotatedMethod<?>> cast(from));
            }
            if (from instanceof AnnotatedConstructor<?>) {
                return enhancedConstructorLoader.load(Reflections.<AnnotatedConstructor<?>> cast(from));
            }
            throw new IllegalArgumentException(INVALID_ANNOTATED_MEMBER, from);
        }
    }

    private abstract class AbstractEnhancedMemberLoader<A extends AnnotatedMember<?>, W extends EnhancedAnnotatedMember<?, ?, ?>> {

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

    private class EnhancedFieldLoader extends AbstractEnhancedMemberLoader<AnnotatedField<?>, EnhancedAnnotatedField<?, ?>> {
        @Override
        public boolean equals(EnhancedAnnotatedField<?, ?> member1, AnnotatedField<?> member2) {
            return AnnotatedTypes.compareAnnotatedField(member1, member2);
        }

        @Override
        public Collection<EnhancedAnnotatedField<?, ?>> getMembersOfDeclaringType(AnnotatedField<?> source) {
            return Reflections.cast(transformer.getEnhancedAnnotatedType(source.getDeclaringType()).getDeclaredEnhancedFields());
        }
    }

    private class EnhancedMethodLoader extends AbstractEnhancedMemberLoader<AnnotatedMethod<?>, EnhancedAnnotatedMethod<?, ?>> {
        @Override
        public boolean equals(EnhancedAnnotatedMethod<?, ?> member1, AnnotatedMethod<?> member2) {
            return AnnotatedTypes.compareAnnotatedCallable(member1, member2);
        }

        @Override
        public Collection<EnhancedAnnotatedMethod<?, ?>> getMembersOfDeclaringType(AnnotatedMethod<?> source) {
            return Reflections.cast(transformer.getEnhancedAnnotatedType(source.getDeclaringType()).getDeclaredEnhancedMethods());
        }
    }

    private class EnhancedConstructorLoader extends AbstractEnhancedMemberLoader<AnnotatedConstructor<?>, EnhancedAnnotatedConstructor<?>> {
        @Override
        public boolean equals(EnhancedAnnotatedConstructor<?> member1, AnnotatedConstructor<?> member2) {
            return AnnotatedTypes.compareAnnotatedCallable(member1, member2);
        }

        @Override
        public Collection<EnhancedAnnotatedConstructor<?>> getMembersOfDeclaringType(AnnotatedConstructor<?> source) {
            return Reflections.cast(transformer.getEnhancedAnnotatedType(source.getDeclaringType()).getEnhancedConstructors());
        }
    }

    public void cleanupAfterBoot() {
        enhancedMemberCache.clear();
    }

    @Override
    public void cleanup() {
        cleanupAfterBoot();
        backedMemberCache.clear();
        unbackedAnnotatedMembersById.clear();
    }
}
