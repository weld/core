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
package org.jboss.weld.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InterceptionType;

import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.manager.BeanManagerImpl;

public class InterceptorResolvableBuilder extends ResolvableBuilder {

    public InterceptorResolvableBuilder(final BeanManagerImpl manager) {
        super(manager);
    }

    public InterceptorResolvableBuilder(final Type type, final BeanManagerImpl manager) {
        super(type, manager);
    }

    private InterceptionType interceptionType;

    @Override
    protected void checkQualifier(Annotation qualifier, final QualifierInstance qualifierInstance,
            Class<? extends Annotation> annotationType) {
        if (!getMetaAnnotationStore().getInterceptorBindingModel(annotationType).isValid()) {
            throw BeanManagerLogger.LOG.interceptorResolutionWithNonbindingType(qualifier);
        }
        if (qualifierInstances.contains(qualifierInstance)) {
            throw BeanManagerLogger.LOG.duplicateInterceptorBinding(qualifierInstance);
        }
    }

    public InterceptorResolvableBuilder setInterceptionType(InterceptionType interceptionType) {
        this.interceptionType = interceptionType;
        return this;
    }

    @Override
    public InterceptorResolvableBuilder addQualifier(Annotation qualifier) {
        super.addQualifier(qualifier);
        return this;
    }

    @Override
    public InterceptorResolvableBuilder addQualifiers(Annotation[] qualifiers) {
        super.addQualifiers(qualifiers);
        return this;
    }

    @Override
    public InterceptorResolvableBuilder addQualifiers(Collection<Annotation> qualifiers) {
        super.addQualifiers(qualifiers);
        return this;
    }

    @Override
    public InterceptorResolvableBuilder addType(Type type) {
        super.addType(type);
        return this;
    }

    @Override
    public InterceptorResolvableBuilder addTypes(Set<Type> types) {
        super.addTypes(types);
        return this;
    }

    @Override
    public InterceptorResolvableBuilder setDeclaringBean(Bean<?> declaringBean) {
        super.setDeclaringBean(declaringBean);
        return this;
    }

    @Override
    public InterceptorResolvable create() {
        if (qualifierInstances.isEmpty()) {
            throw BeanManagerLogger.LOG.interceptorBindingsEmpty();
        }
        return new InterceptorResolvableImpl(rawType, types, declaringBean, interceptionType, qualifierInstances);
    }

    private static class InterceptorResolvableImpl extends ResolvableImpl implements InterceptorResolvable {
        private final InterceptionType interceptionType;

        private InterceptorResolvableImpl(Class<?> rawType, Set<Type> typeClosure, Bean<?> declaringBean,
                InterceptionType interceptionType, final Set<QualifierInstance> instances) {
            super(rawType, typeClosure, declaringBean, instances, false);
            this.interceptionType = interceptionType;
        }

        public InterceptionType getInterceptionType() {
            return interceptionType;
        }

        public int hashCode() {
            return 31 * super.hashCode()
                    + this.getInterceptionType().hashCode();
        }

        public boolean equals(Object o) {
            if (o instanceof Resolvable) {
                Resolvable r = (Resolvable) o;
                return super.equals(r)
                        && r instanceof InterceptorResolvable
                        && this.getInterceptionType().equals(((InterceptorResolvable) r).getInterceptionType());
            }
            return false;
        }
    }

}
