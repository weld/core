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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.interceptor.CdiInterceptorFactory;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.interceptor.proxy.InterceptorMethodInvocation;
import org.jboss.weld.interceptor.proxy.WeldInvocationContext;
import org.jboss.weld.interceptor.reader.InterceptorMetadataImpl;
import org.jboss.weld.interceptor.reader.InterceptorMetadataUtils;
import org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.ReflectionLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Interceptors;
import org.jboss.weld.util.reflection.Formats;

/**
 * @author Marius Bogoevici
 */
public class InterceptorImpl<T> extends ManagedBean<T> implements Interceptor<T> {

    private final InterceptorClassMetadata<T> interceptorMetadata;

    private final Set<Annotation> interceptorBindingTypes;

    private final boolean serializable;

    public static <T> InterceptorImpl<T> of(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> type, BeanManagerImpl beanManager) {
        return new InterceptorImpl<T>(attributes, type, beanManager);
    }

    protected InterceptorImpl(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> type, BeanManagerImpl beanManager) {
        super(attributes, type, new StringBeanIdentifier(forInterceptor(type)), beanManager);
        this.interceptorMetadata = initInterceptorMetadata();
        this.serializable = type.isSerializable();
        this.interceptorBindingTypes = Collections.unmodifiableSet(new HashSet<Annotation>(Interceptors.mergeBeanInterceptorBindings(beanManager, getEnhancedAnnotated(), getStereotypes()).values()));

        if (Beans.findInterceptorBindingConflicts(beanManager, interceptorBindingTypes)) {
            throw new DeploymentException(BeanLogger.LOG.conflictingInterceptorBindings(getType()));
        }
    }

    @SuppressWarnings("unchecked")
    private InterceptorClassMetadata<T> initInterceptorMetadata() {
        CdiInterceptorFactory<T> reference = new CdiInterceptorFactory<T>(this);
        return new InterceptorMetadataImpl<T>((Class<T>) getBeanClass(), reference, InterceptorMetadataUtils.buildMethodMap(getEnhancedAnnotated(), false, getBeanManager()));
    }

    @Override
    public Set<Annotation> getInterceptorBindings() {
        return interceptorBindingTypes;
    }

    public InterceptorClassMetadata<T> getInterceptorMetadata() {
        return interceptorMetadata;
    }

    @Override
    public Object intercept(InterceptionType type, T instance, final InvocationContext ctx) {
        final org.jboss.weld.interceptor.spi.model.InterceptionType interceptionType = org.jboss.weld.interceptor.spi.model.InterceptionType.valueOf(type.name());
        final List<InterceptorMethodInvocation> methodInvocations = interceptorMetadata.getInterceptorInvocation(instance, interceptionType).getInterceptorMethodInvocations();

        try {
            /*
             * Calling Interceptor.intercept() may result in multiple interceptor method invocations (provided the interceptor class
             * interceptor methods on superclasses). This requires cooperation with InvocationContext.
             *
             * We use a wrapper InvocationContext for the purpose of executing the chain of
             * interceptor methods of this interceptor.
             */
            return new WeldInvocationContext(ctx, methodInvocations).proceed();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    @Override
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
        if (interceptorMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.POST_CONSTRUCT)
                || interceptorMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.PRE_DESTROY)
                || interceptorMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.POST_ACTIVATE)
                || interceptorMetadata.isEligible(org.jboss.weld.interceptor.spi.model.InterceptionType.PRE_PASSIVATE)) {
            for (Annotation interceptorBindingType : interceptorBindingTypes) {
                Target target = interceptorBindingType.annotationType().getAnnotation(Target.class);
                if (target == null || hasInvalidTargetType(target.value())) {
                    throw ReflectionLogger.LOG.lifecycleCallbackInterceptorWithInvalidBindingTarget(this, interceptorBindingType.annotationType().getName(),
                            target != null ? Arrays.toString(target.value()) : "Target meta-annotation is not present");
                }
            }
        }
    }

    private boolean hasInvalidTargetType(ElementType[] elementTypes) {
        for (ElementType elementType : elementTypes) {
            if (!ElementType.TYPE.equals(elementType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Interceptor [" + getBeanClass() + " intercepts " + Formats.formatAnnotations(getInterceptorBindings()) + "]";
    }
}
