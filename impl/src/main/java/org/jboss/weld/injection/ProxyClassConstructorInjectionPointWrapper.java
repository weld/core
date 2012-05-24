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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.bean.proxy.BeanInstance;
import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * A wrapper on a {@link ConstructorInjectionPoint}, to be used if a proxy subclass is instantiated instead of the
 * original (e.g. because the original is an abstract {@link javax.decorator.Decorator})
 * <p/>
 *
 * This class is immutable.
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author Jozef Hartinger
 */
// TODO Needs equals/hashcode
// TODO Would be clearer to make this either a wrapper or not
public class ProxyClassConstructorInjectionPointWrapper<T> extends ConstructorInjectionPoint<T> {
    private ConstructorInjectionPoint<T> originalConstructorInjectionPoint;
    private final boolean decorator;
    private final int delegateInjectionPointPosition;
    private final Bean<?> bean;

    public ProxyClassConstructorInjectionPointWrapper(Bean<T> declaringBean, Class<?> declaringComponentClass, EnhancedAnnotatedConstructor<T> weldConstructor, ConstructorInjectionPoint<T> originalConstructorInjectionPoint, BeanManagerImpl manager) {
        super(weldConstructor, declaringBean, declaringComponentClass, InjectionPointFactory.silentInstance(), manager);
        this.decorator = (declaringBean instanceof javax.enterprise.inject.spi.Decorator);
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
    public T newInstance(BeanManagerImpl manager, CreationalContext<?> creationalContext, Object[] parameterValues) {
        // Once the instance is created, a method handler is required regardless of whether
        // an actual bean instance is known yet.
        final T instance = super.newInstance(manager, creationalContext, parameterValues);
        if (decorator) {
            BeanInstance beanInstance = null;
            if (hasDelegateInjectionPoint()) {
                Object decoratorDelegate = parameterValues[delegateInjectionPointPosition];
                beanInstance = new TargetBeanInstance(decoratorDelegate);
            }
            ProxyFactory.setBeanInstance(instance, beanInstance, bean);
        } else {
            if (instance instanceof ProxyObject) {
                ((ProxyObject) instance).setHandler(new CombinedInterceptorAndDecoratorStackMethodHandler());
            }
        }
        return instance;
    }
}
