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

package org.jboss.weld.injection;

import java.util.List;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.bean.proxy.BeanInstance;
import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.InterceptedSubclassFactory;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * A wrapper on a {@link ConstructorInjectionPoint}, to be used if a proxy subclass is instantiated instead of the
 * original (e.g. because the original is an abstract {@link jakarta.decorator.Decorator})
 * <p/>
 *
 * This class is immutable.
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author Jozef Hartinger
 */
public class ProxyClassConstructorInjectionPointWrapper<T> extends ConstructorInjectionPoint<T> {
    private final ConstructorInjectionPoint<T> originalConstructorInjectionPoint;
    private final boolean decorator;
    private final int delegateInjectionPointPosition;
    private final Bean<?> bean;
    private final String contextId;

    public ProxyClassConstructorInjectionPointWrapper(Bean<T> declaringBean, Class<?> declaringComponentClass,
            EnhancedAnnotatedConstructor<T> weldConstructor, ConstructorInjectionPoint<T> originalConstructorInjectionPoint,
            BeanManagerImpl manager) {
        super(weldConstructor, declaringBean, declaringComponentClass, InjectionPointFactory.silentInstance(), manager);
        this.contextId = manager.getContextId();
        this.decorator = (declaringBean instanceof jakarta.enterprise.inject.spi.Decorator);
        this.originalConstructorInjectionPoint = originalConstructorInjectionPoint;
        this.bean = declaringBean;
        this.delegateInjectionPointPosition = initDelegateInjectionPointPosition();
    }

    private int initDelegateInjectionPointPosition() {
        for (ParameterInjectionPoint<?, T> parameter : getParameterInjectionPoints()) {
            if (parameter.isDelegate()) {
                return parameter.getAnnotated().getPosition();
            }
        }
        return -1;
    }

    private boolean hasDelegateInjectionPoint() {
        return delegateInjectionPointPosition != -1;
    }

    @Override
    public List<ParameterInjectionPoint<?, T>> getParameterInjectionPoints() {
        return originalConstructorInjectionPoint.getParameterInjectionPoints();
    }

    @Override
    protected T newInstance(Object[] parameterValues) {
        // Once the instance is created, a method handler is required regardless of whether
        // an actual bean instance is known yet.
        final T instance = super.newInstance(parameterValues);
        if (decorator) {
            BeanInstance beanInstance = null;
            if (hasDelegateInjectionPoint()) {
                Object decoratorDelegate = parameterValues[delegateInjectionPointPosition];
                beanInstance = new TargetBeanInstance(decoratorDelegate);
            }
            ProxyFactory.setBeanInstance(contextId, instance, beanInstance, bean);
        } else {
            if (instance instanceof ProxyObject) {
                ((ProxyObject) instance).weld_setHandler(new CombinedInterceptorAndDecoratorStackMethodHandler());
                // Set method handler for private methods if necessary
                InterceptedSubclassFactory.setPrivateMethodHandler(instance);
            }
        }
        return instance;
    }

    @Override
    public AnnotatedConstructor<T> getComponentConstructor() {
        return originalConstructorInjectionPoint.getAnnotated();
    }

    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((originalConstructorInjectionPoint == null) ? 0 : originalConstructorInjectionPoint.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProxyClassConstructorInjectionPointWrapper<?> other = (ProxyClassConstructorInjectionPointWrapper<?>) obj;
        if (originalConstructorInjectionPoint == null) {
            if (other.originalConstructorInjectionPoint != null) {
                return false;
            }
        } else if (!originalConstructorInjectionPoint.equals(other.originalConstructorInjectionPoint)) {
            return false;
        }
        return true;
    }

}
