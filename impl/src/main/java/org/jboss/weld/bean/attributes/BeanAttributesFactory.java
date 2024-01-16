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
package org.jboss.weld.bean.attributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMember;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MergedStereotypes;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;

/**
 * Creates {@link BeanAttributes} based on a given annotated.
 *
 * @author Jozef Hartinger
 */
public class BeanAttributesFactory {

    private static final Set<Annotation> DEFAULT_QUALIFIERS = ImmutableSet.of(Any.Literal.INSTANCE, Default.Literal.INSTANCE);

    private BeanAttributesFactory() {
    }

    /**
     * Creates new {@link BeanAttributes} to represent a managed bean.
     */
    public static <T> BeanAttributes<T> forBean(EnhancedAnnotated<T, ?> annotated, BeanManagerImpl manager) {
        return new BeanAttributesBuilder<T>(annotated, manager).build();
    }

    public static class BeanAttributesBuilder<T> {

        private MergedStereotypes<T, ?> mergedStereotypes;
        private boolean alternative;
        private String name;
        private Set<Annotation> qualifiers;
        private Set<Type> types;
        private Class<? extends Annotation> scope;
        private BeanManagerImpl manager;
        protected final EnhancedAnnotated<T, ?> annotated;

        public BeanAttributesBuilder(EnhancedAnnotated<T, ?> annotated, Set<Type> types, BeanManagerImpl manager) {
            this.manager = manager;
            this.annotated = annotated;
            initStereotypes(annotated, manager);
            initAlternative(annotated);
            initName(annotated);
            initQualifiers(annotated);
            initScope(annotated);
            this.types = types;
        }

        public BeanAttributesBuilder(EnhancedAnnotated<T, ?> annotated, BeanManagerImpl manager) {
            this(annotated, SharedObjectCache.instance(manager).getSharedSet(Beans.getTypes(annotated)), manager);
        }

        protected <S> void initStereotypes(EnhancedAnnotated<T, S> annotated, BeanManagerImpl manager) {
            this.mergedStereotypes = MergedStereotypes.of(annotated, manager);
        }

        protected void initAlternative(EnhancedAnnotated<T, ?> annotated) {
            this.alternative = Beans.isAlternative(annotated, mergedStereotypes);
        }

        /**
         * Initializes the name
         */
        protected void initName(EnhancedAnnotated<T, ?> annotated) {
            boolean beanNameDefaulted = false;
            if (annotated.isAnnotationPresent(Named.class)) {
                String javaName = annotated.getAnnotation(Named.class).value();
                if ("".equals(javaName)) {
                    beanNameDefaulted = true;
                } else {
                    this.name = javaName;
                    return;
                }
            }
            if (beanNameDefaulted || (mergedStereotypes != null && mergedStereotypes.isBeanNameDefaulted())) {
                this.name = getDefaultName(annotated);
            }
        }

        /**
         * Gets the default name of the bean
         *
         * @return The default name
         */
        protected String getDefaultName(EnhancedAnnotated<?, ?> annotated) {
            if (annotated instanceof EnhancedAnnotatedType<?>) {
                StringBuilder defaultName = new StringBuilder(((EnhancedAnnotatedType<?>) annotated).getSimpleName());
                defaultName.setCharAt(0, Character.toLowerCase(defaultName.charAt(0)));
                return defaultName.toString();
            } else if (annotated instanceof EnhancedAnnotatedField<?, ?>) {
                return ((EnhancedAnnotatedField<?, ?>) annotated).getPropertyName();
            } else if (annotated instanceof EnhancedAnnotatedMethod<?, ?>) {
                return ((EnhancedAnnotatedMethod<?, ?>) annotated).getPropertyName();
            } else {
                return null;
            }
        }

        protected void initQualifiers(Set<Annotation> qualifiers) {
            if (qualifiers.isEmpty()) {
                this.qualifiers = DEFAULT_QUALIFIERS;
            } else {
                Set<Annotation> normalizedQualifiers = new HashSet<Annotation>(qualifiers.size() + 2);
                if (qualifiers.size() == 1) {
                    Annotation qualifier = qualifiers.iterator().next();
                    if (qualifier.annotationType().equals(Named.class) || qualifier.equals(Any.Literal.INSTANCE)) {
                        // Single qualifier - @Named or @Any
                        normalizedQualifiers.add(Default.Literal.INSTANCE);
                    }
                } else if (qualifiers.size() == 2 && qualifiers.contains(Any.Literal.INSTANCE)) {
                    for (Annotation qualifier : qualifiers) {
                        if (qualifier.annotationType().equals(Named.class)) {
                            // Two qualifiers - @Named and @Any
                            normalizedQualifiers.add(Default.Literal.INSTANCE);
                            break;
                        }
                    }
                }
                normalizedQualifiers.addAll(qualifiers);
                normalizedQualifiers.add(Any.Literal.INSTANCE);
                if (name != null && normalizedQualifiers.remove(NamedLiteral.DEFAULT)) {
                    normalizedQualifiers.add(new NamedLiteral(name));
                }
                this.qualifiers = SharedObjectCache.instance(manager).getSharedSet(normalizedQualifiers);
            }
        }

        protected void initQualifiers(EnhancedAnnotated<?, ?> annotated) {
            initQualifiers(annotated.getMetaAnnotations(Qualifier.class));
        }

        protected void initScope(EnhancedAnnotated<T, ?> annotated) {
            // class bean
            if (annotated instanceof EnhancedAnnotatedType<?>) {
                EnhancedAnnotatedType<?> weldClass = (EnhancedAnnotatedType<?>) annotated;
                for (EnhancedAnnotatedType<?> clazz = weldClass; clazz != null; clazz = clazz.getEnhancedSuperclass()) {
                    Set<Annotation> scopes = new HashSet<Annotation>();
                    scopes.addAll(clazz.getDeclaredMetaAnnotations(Scope.class));
                    scopes.addAll(clazz.getDeclaredMetaAnnotations(NormalScope.class));
                    validateScopeSet(scopes, annotated);
                    if (scopes.size() == 1) {
                        if (annotated.isAnnotationPresent(scopes.iterator().next().annotationType())) {
                            this.scope = scopes.iterator().next().annotationType();
                        }
                        break;
                    }
                }
            } else {
                // producer field or method
                Set<Annotation> scopes = new HashSet<Annotation>();
                scopes.addAll(annotated.getMetaAnnotations(Scope.class));
                scopes.addAll(annotated.getMetaAnnotations(NormalScope.class));
                if (scopes.size() == 1) {
                    this.scope = scopes.iterator().next().annotationType();
                }
                validateScopeSet(scopes, annotated);
            }

            if (this.scope == null) {
                initScopeFromStereotype();
            }

            if (this.scope == null) {
                this.scope = Dependent.class;
            }
        }

        protected void validateScopeSet(Set<Annotation> scopes, EnhancedAnnotated<T, ?> annotated) {
            if (scopes.size() > 1) {
                throw BeanLogger.LOG.onlyOneScopeAllowed(annotated);
            }
        }

        protected boolean initScopeFromStereotype() {
            Set<Annotation> possibleScopes = mergedStereotypes.getPossibleScopes();
            if (possibleScopes.size() == 1) {
                this.scope = possibleScopes.iterator().next().annotationType();
                return true;
            } else if (possibleScopes.size() > 1) {
                String stack;
                Class<?> declaringClass;
                if (annotated instanceof EnhancedAnnotatedMember) {
                    EnhancedAnnotatedMember<?, ?, ?> member = (EnhancedAnnotatedMember<?, ?, ?>) annotated;
                    declaringClass = member.getJavaMember().getDeclaringClass();
                    stack = "\n  at " + Formats.formatAsStackTraceElement(member.getJavaMember());
                } else {
                    declaringClass = annotated.getJavaClass();
                    stack = "";
                }
                throw BeanLogger.LOG.multipleScopesFoundFromStereotypes(Formats.formatType(declaringClass, false),
                        Formats.formatTypes(mergedStereotypes.getStereotypes(), false), possibleScopes, stack);
            } else {
                return false;
            }
        }

        public BeanAttributes<T> build() {
            return new ImmutableBeanAttributes<T>(mergedStereotypes.getStereotypes(), alternative, name, qualifiers, types,
                    scope);
        }
    }
}
