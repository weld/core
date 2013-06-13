/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.builtin;

import static org.jboss.weld.ContainerState.INITIALIZED;
import static org.jboss.weld.ContainerState.VALIDATED;
import static org.jboss.weld.logging.messages.BeanManagerMessage.METHOD_NOT_AVAILABLE_DURING_INITIALIZATION;

import java.io.ObjectStreamException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.SystemPropertiesConfiguration;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.ForwardingBeanManager;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Client view of {@link BeanManagerImpl}.
 *
 * @author Martin Kouba
 */
public class BeanManagerProxy extends ForwardingBeanManager {

    private static final String GET_BEANS_METHOD_NAME = "getBeans()";

    private static final long serialVersionUID = -6990849486568169846L;

    private final BeanManagerImpl manager;
    private transient volatile Container container;

    public BeanManagerProxy(BeanManagerImpl manager) {
        this.manager = manager;
    }

    @Override
    public BeanManagerImpl delegate() {
        return manager;
    }

    @Override
    public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
        checkContainerValidated("getReference()");
        return super.getReference(bean, beanType, ctx);
    }

    @Override
    public Object getInjectableReference(InjectionPoint ij, CreationalContext<?> ctx) {
        checkContainerValidated("getInjectableReference()");
        return super.getInjectableReference(ij, ctx);
    }

    @Override
    public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
        checkContainerValidated(GET_BEANS_METHOD_NAME);
        return super.getBeans(beanType, qualifiers);
    }

    @Override
    public Set<Bean<?>> getBeans(String name) {
        checkContainerValidated(GET_BEANS_METHOD_NAME);
        return super.getBeans(name);
    }

    @Override
    public Bean<?> getPassivationCapableBean(String id) {
        checkContainerValidated("getPassivationCapableBean()");
        return super.getPassivationCapableBean(id);
    }

    @Override
    public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
        checkContainerValidated("resolve()");
        return super.resolve(beans);
    }

    @Override
    public void validate(InjectionPoint injectionPoint) {
        checkContainerValidated("validate()");
        super.validate(injectionPoint);
    }

    @Override
    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... qualifiers) {
        checkContainerValidated("resolveObserverMethods()");
        return super.resolveObserverMethods(event, qualifiers);
    }

    @Override
    public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers) {
        checkContainerValidated("resolveDecorators()");
        return super.resolveDecorators(types, qualifiers);
    }

    @Override
    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
        checkContainerValidated("resolveInterceptors()");
        return super.resolveInterceptors(type, interceptorBindings);
    }

    protected Object readResolve() throws ObjectStreamException {
        return new BeanManagerProxy(this.manager);
    }

    /**
     * When in portable mode (default) this method verifies that the container has been validated. If it hasn't been, an
     * {@link IllegalStateException} is thrown. When in non-portable mode this method is no-op.
     *
     * @param methodName
     * @throws IllegalStateException If the application initialization is not finished yet
     */
    private void checkContainerValidated(String methodName) {
        if (SystemPropertiesConfiguration.INSTANCE.isNonPortableModeEnabled()) {
            return;
        }
        if (this.container == null) {
            this.container = Container.instance();
        }
        ContainerState state = container.getState();
        if (!INITIALIZED.equals(state) && !VALIDATED.equals(state)) {
            throw new IllegalStateException(METHOD_NOT_AVAILABLE_DURING_INITIALIZATION, methodName);
        }
    }

    public static BeanManagerImpl unwrap(BeanManager manager) {
        if (manager instanceof ForwardingBeanManager) {
            manager = Reflections.<ForwardingBeanManager> cast(manager).delegate();
        }
        if (manager instanceof BeanManagerImpl) {
            return (BeanManagerImpl) manager;
        }
        throw new IllegalArgumentException("Unknown BeanManager " + manager);
    }

}
