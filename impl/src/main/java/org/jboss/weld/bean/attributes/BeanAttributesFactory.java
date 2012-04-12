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
package org.jboss.weld.bean.attributes;

import static org.jboss.weld.logging.messages.BeanMessage.MULTIPLE_SCOPES_FOUND_FROM_STEREOTYPES;
import static org.jboss.weld.logging.messages.BeanMessage.ONLY_ONE_SCOPE_ALLOWED;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MergedStereotypes;
import org.jboss.weld.resources.SharedObjectFacade;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Creates {@link BeanAttributes} based on a given annotated.
 *
 * @author Jozef Hartinger
 */
public class BeanAttributesFactory {

    private static final Set<Annotation> DEFAULT_QUALIFIERS = Collections.unmodifiableSet(new ArraySet<Annotation>(AnyLiteral.INSTANCE, DefaultLiteral.INSTANCE).trimToSize());

    /**
     * Creates new {@link BeanAttributes} to represent a managed bean.
     */
    public static <T> BeanAttributes<T> forBean(EnhancedAnnotated<T, ?> annotated, BeanManagerImpl manager) {
        return new BeanAttributesBuilder<T>(annotated, null, manager).build();
    }

    /**
     * Creates new {@link BeanAttributes} to represent a session bean.
     */
    public static <T> BeanAttributes<T> forSessionBean(EnhancedAnnotatedType<T> annotated, InternalEjbDescriptor<?> descriptor, BeanManagerImpl manager) {
        return new BeanAttributesBuilder<T>(annotated, Reflections.<InternalEjbDescriptor<T>> cast(descriptor), manager).build();
    }

    /**
     * Creates new {@link BeanAttributes} to represent a disposer method.
     */
    public static <T, X> BeanAttributes<T> forDisposerMethod(EnhancedAnnotatedMethod<T, ? super X> annotated, BeanManagerImpl manager) {
        BeanAttributesBuilder<T> builder = new BeanAttributesBuilder<T>();
        EnhancedAnnotatedParameter<?, ?> disposerParameter = null;
        if (annotated.getEnhancedParameters(Disposes.class).isEmpty()) {
            throw new IllegalArgumentException("Not a disposer method");
        }
        disposerParameter = annotated.getEnhancedParameters().get(0);
        builder.nullable = false; // not relevant
        builder.initStereotypes(annotated, manager);
        builder.initAlternative(annotated);
        builder.name = null; // not relevant
        builder.qualifiers = disposerParameter.getMetaAnnotations(Qualifier.class);
        builder.scope = null;
        builder.types = disposerParameter.getTypeClosure();
        return builder.build();
    }

    public static <T> BeanAttributes<T> forNewBean(boolean nullable, Set<Type> types) {
        return new ImmutableBeanAttributes<T>(nullable, Collections.<Class<? extends Annotation>> emptySet(), false, null, Collections.<Annotation> emptySet(), types,
                Dependent.class);
    }

    public static <T> BeanAttributes<T> forNewManagedBean(EnhancedAnnotatedType<T> weldClass) {
        return forNewBean(Beans.isNullable(weldClass), SharedObjectFacade.wrap(Beans.getTypes(weldClass)));
    }

    public static <T> BeanAttributes<T> forNewSessionBean(BeanAttributes<T> originalAttributes) {
        return forNewBean(originalAttributes.isNullable(), originalAttributes.getTypes());
    }

    private static class BeanAttributesBuilder<T> {

        private boolean nullable;
        private MergedStereotypes<T, ?> mergedStereotypes;
        private boolean alternative;
        private String name;
        private Set<Annotation> qualifiers;
        private Set<Type> types;
        private Class<? extends Annotation> scope;

        private BeanAttributesBuilder() {
        }

        private BeanAttributesBuilder(EnhancedAnnotated<T, ?> annotated, InternalEjbDescriptor<T> descriptor, BeanManagerImpl manager) {
            initNullable(annotated);
            initStereotypes(annotated, manager);
            initAlternative(annotated);
            initName(annotated);
            initQualifiers(annotated);
            initScope(annotated);
            if (descriptor == null) {
                types = SharedObjectFacade.wrap(Beans.getTypes(annotated));
            } else {
                types = SharedObjectFacade.wrap(Beans.getTypes(annotated, descriptor));
            }
        }

        protected void initNullable(EnhancedAnnotated<?, ?> annotated) {
            this.nullable = !annotated.isPrimitive();
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
                return Introspector.decapitalize(((EnhancedAnnotatedType<?>) annotated).getSimpleName());
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
                ArraySet<Annotation> normalizedQualifiers = new ArraySet<Annotation>(qualifiers.size() + 2);
                if (qualifiers.size() == 1) {
                    if (qualifiers.iterator().next().annotationType().equals(Named.class)) {
                        normalizedQualifiers.add(DefaultLiteral.INSTANCE);
                    }
                }
                normalizedQualifiers.addAll(qualifiers);
                normalizedQualifiers.add(AnyLiteral.INSTANCE);
                if (name != null && normalizedQualifiers.remove(NamedLiteral.DEFAULT)) {
                    normalizedQualifiers.add(new NamedLiteral(name));
                }
                this.qualifiers = SharedObjectFacade.wrap(normalizedQualifiers.trimToSize());
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
                throw new DefinitionException(ONLY_ONE_SCOPE_ALLOWED, annotated);
            }
        }

        protected boolean initScopeFromStereotype() {
            Set<Annotation> possibleScopes = mergedStereotypes.getPossibleScopes();
            if (possibleScopes.size() == 1) {
                this.scope = possibleScopes.iterator().next().annotationType();
                return true;
            } else if (possibleScopes.size() > 1) {
                throw new DefinitionException(MULTIPLE_SCOPES_FOUND_FROM_STEREOTYPES, mergedStereotypes);
            } else {
                return false;
            }
        }

        public BeanAttributes<T> build() {
            return new ImmutableBeanAttributes<T>(nullable, mergedStereotypes.getStereotypes(), alternative, name, qualifiers, types, scope);
        }
    }
}
