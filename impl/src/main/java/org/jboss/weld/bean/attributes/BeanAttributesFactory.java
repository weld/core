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
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;

import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMember;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MergedStereotypes;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Creates {@link BeanAttributes} based on a given annotated.
 *
 * @author Jozef Hartinger
 */
public class BeanAttributesFactory {

    /**
     * Creates new {@link BeanAttributes} to represent a managed bean.
     */
    public static <T> BeanAttributes<T> forManagedBean(WeldClass<T> annotated, BeanManagerImpl manager) {
        return forSessionBean(annotated, null, manager);
    }

    /**
     * Creates new {@link BeanAttributes} to represent a session bean.
     */
    public static <T> BeanAttributes<T> forSessionBean(WeldClass<T> annotated, InternalEjbDescriptor<?> descriptor, BeanManagerImpl manager) {
        return new BeanAttributesBuilder<T>(annotated, null, Reflections.<InternalEjbDescriptor<T>> cast(descriptor), manager).build();
    }

    /**
     * Creates new {@link BeanAttributes} to represent a producer bean.
     */
    public static <T, X, S extends Member> BeanAttributes<T> forProducerBean(WeldMember<T, ? super X, S> annotated, BeanAttributes<X> declaringBeanAttributes,
            BeanManagerImpl manager) {
        return new BeanAttributesBuilder<T>(annotated, declaringBeanAttributes, null, manager).build();
    }

    /**
     * Creates new {@link BeanAttributes} to represent a disposer method.
     */
    public static <T, X> BeanAttributes<T> forDisposerMethod(WeldMethod<T, ? super X> annotated, BeanAttributes<X> declaringBeanAttributes, BeanManagerImpl manager) {
        BeanAttributesBuilder<T> builder = new BeanAttributesBuilder<T>();
        WeldParameter<?, ?> disposerParameter = null;
        if (annotated.getWeldParameters(Disposes.class).isEmpty()) {
            throw new IllegalArgumentException("Not a disposer method");
        }
        disposerParameter = annotated.getWeldParameters().get(0);
        builder.nullable = false; // not relevant
        builder.initStereotypes(annotated, manager);
        builder.initAlternative(annotated, declaringBeanAttributes);
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

    public static <T> BeanAttributes<T> forNewManagedBean(WeldClass<T> weldClass) {
        return forNewBean(Beans.isNullable(weldClass), Beans.getTypes(weldClass));
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

        private BeanAttributesBuilder(WeldAnnotated<T, ?> annotated, BeanAttributes<?> declaringBeanAttributes, InternalEjbDescriptor<T> descriptor, BeanManagerImpl manager) {
            initNullable(annotated);
            initStereotypes(annotated, manager);
            initAlternative(annotated, declaringBeanAttributes);
            initName(annotated);
            initQualifiers(annotated);
            initScope(annotated);
            if (descriptor == null) {
                types = Beans.getTypes(annotated);
            } else {
                types = Beans.getTypes(annotated, descriptor);
            }
        }

        protected void initNullable(WeldAnnotated<?, ?> annotated) {
            this.nullable = !annotated.isPrimitive();
        }

        protected <S> void initStereotypes(WeldAnnotated<T, S> annotated, BeanManagerImpl manager) {
            this.mergedStereotypes = new MergedStereotypes<T, S>(annotated.getMetaAnnotations(Stereotype.class), manager);
        }

        protected void initAlternative(WeldAnnotated<T, ?> annotated, BeanAttributes<?> declaringBeanAttributes) {
            this.alternative = Beans.isAlternative(annotated, mergedStereotypes) || (declaringBeanAttributes != null && declaringBeanAttributes.isAlternative());
        }

        /**
         * Initializes the name
         */
        protected void initName(WeldAnnotated<T, ?> annotated) {
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
        protected String getDefaultName(WeldAnnotated<?, ?> annotated) {
            if (annotated instanceof WeldClass<?>) {
                return Introspector.decapitalize(((WeldClass<?>) annotated).getSimpleName());
            } else if (annotated instanceof WeldField<?, ?>) {
                return ((WeldField<?, ?>) annotated).getPropertyName();
            } else if (annotated instanceof WeldMethod<?, ?>) {
                return ((WeldMethod<?, ?>) annotated).getPropertyName();
            } else {
                return null;
            }
        }

        protected void initQualifiers(Set<Annotation> qualifiers) {
            this.qualifiers = new HashSet<Annotation>();
            this.qualifiers.addAll(qualifiers);
            applyDefaultQualifiers(this.qualifiers, name);
        }

        protected void initQualifiers(WeldAnnotated<?, ?> annotated) {
            initQualifiers(annotated.getMetaAnnotations(Qualifier.class));
        }

        public static void applyDefaultQualifiers(Set<Annotation> qualifiers, String name) {
            if (qualifiers.size() == 0) {
                qualifiers.add(DefaultLiteral.INSTANCE);
            }
            if (qualifiers.size() == 1) {
                if (qualifiers.iterator().next().annotationType().equals(Named.class)) {
                    qualifiers.add(DefaultLiteral.INSTANCE);
                }
            }
            qualifiers.add(AnyLiteral.INSTANCE);

            if (name != null && qualifiers.remove(NamedLiteral.DEFAULT)) {
                qualifiers.add(new NamedLiteral(name));
            }
        }

        protected void initScope(WeldAnnotated<T, ?> annotated) {
            // class bean
            if (annotated instanceof WeldClass<?>) {
                WeldClass<?> weldClass = (WeldClass<?>) annotated;
                for (WeldClass<?> clazz = weldClass; clazz != null; clazz = clazz.getWeldSuperclass()) {
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

        protected void validateScopeSet(Set<Annotation> scopes, WeldAnnotated<T, ?> annotated) {
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
