/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.resources;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedCallable;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMember;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedMember;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedMember;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedType;
import org.jboss.weld.annotated.slim.unbacked.UnbackedMemberIdentifier;
import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Serves several functions. Firstly, transforms a given {@link AnnotatedMember} into its richer counterpart
 * {@link EnhancedAnnotatedMember}. Secondly, a {@link BackedAnnotatedMember} or {@link UnbackedAnnotatedMember} can be looked
 * up.
 *
 * @author Jozef Hartinger
 *
 */
public class MemberTransformer implements BootstrapService {

    private static class MemberKey<X, A extends AnnotatedMember<X>> {
        private final EnhancedAnnotatedType<X> type;
        private final A member;
        private final int hashCode;

        public MemberKey(EnhancedAnnotatedType<X> type, A member) {
            this.type = type;
            this.member = member;
            this.hashCode = Objects.hash(type, member);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MemberKey) {
                MemberKey<?, ?> that = (MemberKey<?, ?>) obj;
                return Objects.equals(this.type, that.type) && Objects.equals(this.member, that.member);
            }
            return false;
        }
    }

    private final ClassTransformer transformer;

    private final ComputingCache<UnbackedMemberIdentifier<?>, UnbackedAnnotatedMember<?>> unbackedAnnotatedMembersById;

    private final ComputingCache<MemberKey<?, ?>, EnhancedAnnotatedMember<?, ?, ?>> enhancedMemberCache;
    private final EnhancedFieldLoader enhancedFieldLoader;
    private final EnhancedMethodLoader enhancedMethodLoader;
    private final EnhancedConstructorLoader enhancedConstructorLoader;

    public MemberTransformer(ClassTransformer transformer) {
        ComputingCacheBuilder cacheBuilder = ComputingCacheBuilder.newBuilder();
        this.transformer = transformer;
        this.unbackedAnnotatedMembersById = cacheBuilder.build(new UnbackedMemberById());
        this.enhancedFieldLoader = new EnhancedFieldLoader();
        this.enhancedMethodLoader = new EnhancedMethodLoader();
        this.enhancedConstructorLoader = new EnhancedConstructorLoader();
        this.enhancedMemberCache = cacheBuilder.build(new EnhancedMemberLoaderFunction());
    }

    // Unbacked members

    public <X> UnbackedAnnotatedMember<X> getUnbackedMember(UnbackedMemberIdentifier<X> identifier) {
        return unbackedAnnotatedMembersById.getCastValue(identifier);
    }

    /**
     * If an unbacked member is being deserialized it is looked in all the members of the declaring type and cached.
     */
    private static class UnbackedMemberById implements Function<UnbackedMemberIdentifier<?>, UnbackedAnnotatedMember<?>> {

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
                if (id.equals(AnnotatedTypes.createMethodId(method.getJavaMember(), method.getAnnotations(),
                        method.getParameters()))) {
                    return Reflections.cast(method);
                }
            }
            for (AnnotatedConstructor<T> constructor : type.getConstructors()) {
                if (id.equals(AnnotatedTypes.createConstructorId(constructor.getJavaMember(), constructor.getAnnotations(),
                        constructor.getParameters()))) {
                    return cast(constructor);
                }
            }
            throw BeanLogger.LOG.unableToLoadMember(id);
        }
    }

    // Enhanced members

    public <X, A extends EnhancedAnnotatedMember<?, X, ? extends Member>> A loadEnhancedMember(AnnotatedMember<X> member,
            String bdaId) {
        if (member instanceof EnhancedAnnotatedMember<?, ?, ?>) {
            return Reflections.cast(member);
        }
        EnhancedAnnotatedType<X> declaringType = transformer.getEnhancedAnnotatedType(member.getDeclaringType(), bdaId);
        return enhancedMemberCache.getCastValue(new MemberKey<X, AnnotatedMember<X>>(declaringType, member));
    }

    public <X> EnhancedAnnotatedParameter<?, X> loadEnhancedParameter(AnnotatedParameter<X> parameter, String bdaId) {
        if (parameter instanceof EnhancedAnnotatedParameter<?, ?>) {
            return Reflections.cast(parameter);
        }
        EnhancedAnnotatedCallable<?, X, Member> callable = loadEnhancedMember(parameter.getDeclaringCallable(), bdaId);
        return callable.getEnhancedParameters().get(parameter.getPosition());
    }

    private class EnhancedMemberLoaderFunction implements Function<MemberKey<?, ?>, EnhancedAnnotatedMember<?, ?, ?>> {
        @Override
        public EnhancedAnnotatedMember<?, ?, ?> apply(MemberKey<?, ?> from) {
            if (from.member instanceof AnnotatedField<?>) {
                return enhancedFieldLoader.load(Reflections.<MemberKey<?, AnnotatedField<?>>> cast(from));
            }
            if (from.member instanceof AnnotatedMethod<?>) {
                return enhancedMethodLoader.load(Reflections.<MemberKey<?, AnnotatedMethod<?>>> cast(from));
            }
            if (from.member instanceof AnnotatedConstructor<?>) {
                return enhancedConstructorLoader.load(Reflections.<MemberKey<?, AnnotatedConstructor<?>>> cast(from));
            }
            throw BeanLogger.LOG.invalidAnnotatedMember(from);
        }
    }

    private abstract class AbstractEnhancedMemberLoader<A extends AnnotatedMember<?>, W extends EnhancedAnnotatedMember<?, ?, ?>> {

        public W load(MemberKey<?, A> source) {
            return findMatching(getMembersOfDeclaringType(source), source.member);
        }

        public W findMatching(Collection<W> members, A source) {
            for (W member : members) {
                if (equals(member, source)) {
                    return member;
                }
            }
            throw BeanLogger.LOG.unableToLoadMember(source);
        }

        public abstract boolean equals(W member1, A member2);

        public abstract Collection<W> getMembersOfDeclaringType(MemberKey<?, A> source);
    }

    private class EnhancedFieldLoader extends AbstractEnhancedMemberLoader<AnnotatedField<?>, EnhancedAnnotatedField<?, ?>> {
        @Override
        public boolean equals(EnhancedAnnotatedField<?, ?> member1, AnnotatedField<?> member2) {
            return AnnotatedTypes.compareAnnotatedField(member1, member2);
        }

        @Override
        public Collection<EnhancedAnnotatedField<?, ?>> getMembersOfDeclaringType(MemberKey<?, AnnotatedField<?>> source) {
            return cast(source.type.getFields());
        }
    }

    private class EnhancedMethodLoader extends AbstractEnhancedMemberLoader<AnnotatedMethod<?>, EnhancedAnnotatedMethod<?, ?>> {
        @Override
        public boolean equals(EnhancedAnnotatedMethod<?, ?> member1, AnnotatedMethod<?> member2) {
            return AnnotatedTypes.compareAnnotatedCallable(member1, member2);
        }

        @Override
        public Collection<EnhancedAnnotatedMethod<?, ?>> getMembersOfDeclaringType(MemberKey<?, AnnotatedMethod<?>> source) {
            return cast(source.type.getMethods());
        }
    }

    private class EnhancedConstructorLoader
            extends AbstractEnhancedMemberLoader<AnnotatedConstructor<?>, EnhancedAnnotatedConstructor<?>> {
        @Override
        public boolean equals(EnhancedAnnotatedConstructor<?> member1, AnnotatedConstructor<?> member2) {
            return AnnotatedTypes.compareAnnotatedCallable(member1, member2);
        }

        @Override
        public Collection<EnhancedAnnotatedConstructor<?>> getMembersOfDeclaringType(
                MemberKey<?, AnnotatedConstructor<?>> source) {
            return cast(source.type.getConstructors());
        }
    }

    public void cleanupAfterBoot() {
        enhancedMemberCache.clear();
    }

    @Override
    public void cleanup() {
        cleanupAfterBoot();
        unbackedAnnotatedMembersById.clear();
    }
}
