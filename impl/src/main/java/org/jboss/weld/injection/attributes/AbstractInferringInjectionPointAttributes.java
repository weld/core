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
package org.jboss.weld.injection.attributes;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Set;

import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.manager.InjectionTargetFactoryImpl;
import org.jboss.weld.serialization.BeanHolder;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractInferringInjectionPointAttributes<T, S>
        implements WeldInjectionPointAttributes<T, S>, Serializable {

    private static final long serialVersionUID = 7820718127728549436L;

    private final BeanHolder<?> bean;
    private final Set<Annotation> qualifiers;
    private final TypeAttribute typeAttribute;
    private final boolean delegate;
    private final Class<?> declaringComponentClass;
                
    public AbstractInferringInjectionPointAttributes(EnhancedAnnotated<?, ?> annotatedElement, String contextId, Bean<?> bean,
            Set<Annotation> qualifiers, Class<?> declaringComponentClass) {
        this.bean = BeanHolder.of(contextId, bean);
        this.qualifiers = qualifiers;
        this.declaringComponentClass = declaringComponentClass;
        if (bean == null) {
            this.typeAttribute = new NonContextualInjectionPointTypeAttribute(declaringComponentClass);
        } else {
            this.typeAttribute = new BeanInjectionPointTypeAttribute();
        }
        this.delegate = annotatedElement.isAnnotationPresent(Delegate.class);
    }

    @Override
    public Type getType() {
        Type t = typeAttribute.getType();
        if (t instanceof TypeVariable<?>) {
                if (InjectionTargetFactoryImpl.javaClassThreadLocal.get() != null) {
                        Class<?> clazz = InjectionTargetFactoryImpl.javaClassThreadLocal.get();
                        Type[] typeParameters = declaringComponentClass.getTypeParameters();
                        int index = 0;
                        for (int i = 0; i < typeParameters.length; i++) {
                                if (typeParameters[i].getTypeName().equals(t.getTypeName())) {
                                        index = i;
                                        break;
                                }
                        }
                        com.google.common.reflect.TypeToken<?> token = com.google.common.reflect.TypeToken.of(clazz);
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        Type[] typeArgs = ((ParameterizedType) token.getSupertype((Class) declaringComponentClass).getType())
                                        .getActualTypeArguments();
                        t = (Class<?>) typeArgs[index];
                } else {
                        t = ((TypeVariable<?>) t).getBounds()[0];
                }
        }
        return t;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Bean<?> getBean() {
        return bean.get();
    }

    @Override
    public boolean isDelegate() {
        return delegate;
    }

    @Override
    public boolean isTransient() {
        return Reflections.isTransient(getMember());
    }

    @Override
    public abstract Member getMember();

    @Override
    public String toString() {
        return getAnnotated().toString();
    }

    @Override
    public <X extends Annotation> X getQualifier(Class<X> annotationType) {
        X qualifier = getAnnotated().getAnnotation(annotationType);
        if (getQualifiers().contains(qualifier)) {
            return qualifier;
        } else {
            return null;
        }
    }

    private abstract class TypeAttribute implements Serializable {
        private static final long serialVersionUID = -4558590047874880757L;

        private transient volatile Type type;

        public Type getType() {
            if (type == null) {
                this.type = resolveType();
            }
            return type;
        }

        protected abstract Type resolveType();
    }

    @SuppressFBWarnings(value = "SE_INNER_CLASS", justification = "The outer class is always serialized along this inner class.")
    private class BeanInjectionPointTypeAttribute extends TypeAttribute {
        private static final long serialVersionUID = 6927120066961769765L;

        @Override
        protected Type resolveType() {
            return new HierarchyDiscovery(getBean().getBeanClass()).resolveType(getAnnotated().getBaseType());
        }
    }

    @SuppressFBWarnings(value = "SE_INNER_CLASS", justification = "The outer class is always serialized along this inner class.")
    private class NonContextualInjectionPointTypeAttribute extends TypeAttribute {
        private static final long serialVersionUID = 1870361474843082321L;

        private Class<?> componentClass;

        public NonContextualInjectionPointTypeAttribute(Class<?> componentClass) {
            this.componentClass = componentClass;
        }

        @Override
        protected Type resolveType() {
            return new HierarchyDiscovery(componentClass).resolveType(getAnnotated().getBaseType());
        }
    }
}
