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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.bean.interceptor.WeldInterceptorClassMetadata;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.interceptor.proxy.InterceptorInvocation;
import org.jboss.weld.interceptor.proxy.SimpleInterceptionChain;
import org.jboss.weld.interceptor.reader.ClassMetadataInterceptorReference;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Formats;

import static org.jboss.weld.logging.messages.BeanMessage.CONFLICTING_INTERCEPTOR_BINDINGS;
import static org.jboss.weld.logging.messages.BeanMessage.MISSING_BINDING_ON_INTERCEPTOR;

/**
 * @author Marius Bogoevici
 */
public class InterceptorImpl<T> extends ManagedBean<T> implements Interceptor<T> {

    private final InterceptorMetadata<?> interceptorMetadata;

    private final Set<Annotation> interceptorBindingTypes;

    private final boolean serializable;

    public static <T> InterceptorImpl<T> of(BeanAttributes<T> attributes, WeldClass<T> type, BeanManagerImpl beanManager, ServiceRegistry services) {
        return new InterceptorImpl<T>(attributes, type, beanManager, services);
    }

    protected InterceptorImpl(BeanAttributes<T> attributes, WeldClass<T> type, BeanManagerImpl beanManager, ServiceRegistry services) {
        super(attributes, type, new StringBuilder().append(Interceptor.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(type.getName()).toString(), beanManager, services);
        this.interceptorMetadata = beanManager.getInterceptorMetadataReader().getInterceptorMetadata(ClassMetadataInterceptorReference.of(WeldInterceptorClassMetadata.of(type)));
        this.serializable = type.isSerializable();
        this.interceptorBindingTypes = new HashSet<Annotation>(mergeBeanInterceptorBindings(beanManager, getWeldAnnotated(), getStereotypes()).values());

        if (this.interceptorBindingTypes.size() == 0) {
            throw new DeploymentException(MISSING_BINDING_ON_INTERCEPTOR, type.getName());
        }
        if (Beans.findInterceptorBindingConflicts(beanManager, interceptorBindingTypes)) {
            throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, getType());
        }
    }

    public Set<Annotation> getInterceptorBindings() {
        return interceptorBindingTypes;
    }

    public InterceptorMetadata<?> getInterceptorMetadata() {
        return interceptorMetadata;
    }

    public Object intercept(InterceptionType type, T instance, InvocationContext ctx) {
        try {
            org.jboss.weld.interceptor.spi.model.InterceptionType interceptionType = org.jboss.weld.interceptor.spi.model.InterceptionType.valueOf(type.name());
            Collection<InterceptorInvocation<?>> invocations = new ArrayList<InterceptorInvocation<?>>();
            invocations.add(new InterceptorInvocation<T>(instance, interceptorMetadata, interceptionType));
            return new SimpleInterceptionChain(invocations, instance, ctx.getMethod()).invokeNextInterceptor(ctx);
        } catch (Throwable e) {
            throw new WeldException(e);
        }
    }

    public boolean intercepts(InterceptionType type) {
        return interceptorMetadata.getInterceptorMethods(org.jboss.weld.interceptor.spi.model.InterceptionType.valueOf(type.name())).size() > 0;
    }

    public boolean isSerializable() {
        return serializable;
    }

    @Override
    protected void defaultPostConstruct(T instance) {
        // Lifecycle callbacks not supported
    }

    @Override
    protected void defaultPreDestroy(T instance) {
        // Lifecycle callbacks not supported
    }

    @Override
    public String toString() {
        return "Interceptor [" + getBeanClass() + " intercepts " + Formats.formatAnnotations(getInterceptorBindings()) + "]";
    }
}
