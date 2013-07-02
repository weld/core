/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.bean;

import static org.jboss.weld.bean.BeanIdentifiers.forInterceptor;
import static org.jboss.weld.logging.messages.BeanMessage.CONFLICTING_INTERCEPTOR_BINDINGS;
import static org.jboss.weld.logging.messages.BeanMessage.MISSING_BINDING_ON_INTERCEPTOR;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.interceptor.CdiInterceptorFactory;
import org.jboss.weld.bean.interceptor.WeldInterceptorClassMetadata;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.interceptor.proxy.InterceptorInvocation;
import org.jboss.weld.interceptor.proxy.SimpleInterceptionChain;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.logging.messages.ReflectionMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Interceptors;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Formats;

/**
 * @author Marius Bogoevici
 */
public class InterceptorImpl<T> extends ManagedBean<T> implements Interceptor<T> {

    private final InterceptorMetadata<T> interceptorMetadata;

    private final Set<Annotation> interceptorBindingTypes;

    private final boolean serializable;

    public static <T> InterceptorImpl<T> of(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> type, BeanManagerImpl beanManager) {
        return new InterceptorImpl<T>(attributes, type, beanManager);
    }

    protected InterceptorImpl(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> type, BeanManagerImpl beanManager) {
        super(attributes, type, new StringBeanIdentifier(forInterceptor(type)), beanManager);
        this.interceptorMetadata = getInterceptorMetadata(type, this, beanManager);
        this.serializable = type.isSerializable();
        this.interceptorBindingTypes = Collections.unmodifiableSet(new HashSet<Annotation>(Interceptors.mergeBeanInterceptorBindings(beanManager, getEnhancedAnnotated(), getStereotypes()).values()));

        if (this.interceptorBindingTypes.size() == 0) {
            throw new DeploymentException(MISSING_BINDING_ON_INTERCEPTOR, type.getName());
        }
        if (Beans.findInterceptorBindingConflicts(beanManager, interceptorBindingTypes)) {
            throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, getType());
        }
    }

    private static <T> InterceptorMetadata<T> getInterceptorMetadata(EnhancedAnnotatedType<T> type, Interceptor<T> interceptor, BeanManagerImpl manager) {
        ClassMetadata<T> classMetadata = WeldInterceptorClassMetadata.of(type);
        CdiInterceptorFactory<T> reference = new CdiInterceptorFactory<T>(classMetadata, interceptor);
        return manager.getInterceptorMetadataReader().getInterceptorMetadata(reference);
    }

    public Set<Annotation> getInterceptorBindings() {
        return interceptorBindingTypes;
    }

    public InterceptorMetadata<T> getInterceptorMetadata() {
        return interceptorMetadata;
    }

    public Object intercept(InterceptionType type, T instance, InvocationContext ctx) {
        try {
            org.jboss.weld.interceptor.spi.model.InterceptionType interceptionType = org.jboss.weld.interceptor.spi.model.InterceptionType.valueOf(type.name());
            Collection<InterceptorInvocation> invocations = new ArrayList<InterceptorInvocation>();
            invocations.add(interceptorMetadata.getInterceptorInvocation(instance, interceptionType));
            return new SimpleInterceptionChain(invocations).invokeNextInterceptor(ctx);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new WeldException(e);
        }
    }

    public boolean intercepts(InterceptionType type) {
        return interceptorMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.valueOf(type.name()));
    }

    public boolean isSerializable() {
        return serializable;
    }

    @Override
    public void initializeAfterBeanDiscovery() {
        super.initializeAfterBeanDiscovery();
        checkInterceptorBindings();
    }

    private void checkInterceptorBindings() {
        if (interceptorMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.POST_CONSTRUCT) ||
                interceptorMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.PRE_DESTROY) ||
                interceptorMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.POST_ACTIVATE) ||
                interceptorMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.PRE_PASSIVATE)) {
            for (Annotation interceptorBindingType : interceptorBindingTypes) {
                Target target = interceptorBindingType.annotationType().getAnnotation(Target.class);
                if (target != null && Arrays2.unorderedEquals(target.value(), ElementType.TYPE, ElementType.METHOD)) {
                    throw new DefinitionException(ReflectionMessage.METHOD_ELEMENT_TYPE_NOT_ALLOWED, this, interceptorBindingType.annotationType());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Interceptor [" + getBeanClass() + " intercepts " + Formats.formatAnnotations(getInterceptorBindings()) + "]";
    }
}
