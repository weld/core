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
package org.jboss.weld.injection.producer;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.proxy.DecoratorProxy;
import org.jboss.weld.bean.proxy.DecoratorProxyFactory;
import org.jboss.weld.bean.proxy.ProxyMethodHandler;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.attributes.WeldInjectionPointAttributes;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Decorators;
import org.jboss.weld.util.reflection.Reflections;

/**
 * {@link InjectionTarget} implementation used for decorators.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class DecoratorInjectionTarget<T> extends BeanInjectionTarget<T> {

    private final WeldInjectionPointAttributes<?, ?> delegateInjectionPoint;
    private final Field accessibleField;

    public DecoratorInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        super(type, bean, beanManager);
        this.delegateInjectionPoint = Decorators.findDelegateInjectionPoint(type, getInjectionPoints());
        if (delegateInjectionPoint instanceof FieldInjectionPoint<?, ?>) {
            FieldInjectionPoint<?, ?> fip = (FieldInjectionPoint<?, ?>) delegateInjectionPoint;
            this.accessibleField = Reflections.getAccessibleCopyOfMember(fip.getAnnotated().getJavaMember());
        } else {
            this.accessibleField = null;
        }
        checkAbstractMethods(type);
    }

    @Override
    protected Instantiator<T> initInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager,
            Set<InjectionPoint> injectionPoints) {
        if (type.isAbstract()) {
            ConstructorInjectionPoint<T> originalConstructor = InjectionPointFactory.instance()
                    .createConstructorInjectionPoint(bean, type, beanManager);
            injectionPoints.addAll(originalConstructor.getParameterInjectionPoints());
            final WeldInjectionPointAttributes<?, ?> delegateInjectionPoint = Decorators.findDelegateInjectionPoint(type,
                    injectionPoints);
            return new SubclassedComponentInstantiator<T>(type, bean, originalConstructor, beanManager) {
                @Override
                protected Class<T> createEnhancedSubclass(EnhancedAnnotatedType<T> type, Bean<?> bean,
                        BeanManagerImpl manager) {
                    return new DecoratorProxyFactory<T>(manager.getContextId(), type.getJavaClass(), delegateInjectionPoint,
                            bean).getProxyClass();
                }
            };
        } else {
            DefaultInstantiator<T> instantiator = new DefaultInstantiator<T>(type, bean, beanManager);
            injectionPoints.addAll(instantiator.getConstructorInjectionPoint().getParameterInjectionPoints());
            return instantiator;
        }
    }

    @Override
    protected void checkDelegateInjectionPoints() {
        // noop, delegate injection points are checked in Decorators#findDelegateInjectionPoint() called within the constructor
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {
        super.inject(instance, ctx);

        if (accessibleField != null && instance instanceof DecoratorProxy) {

            // this code is only applicable if the delegate is injected into a field
            // as the proxy can't intercept the delegate when setting the field
            // we need to now read the delegate from the field

            // this is only needed for fields, as constructor and method injection are handed
            // at injection time
            Object delegate;
            try {
                delegate = accessibleField.get(instance);
            } catch (IllegalAccessException e) {
                throw UtilLogger.LOG.accessErrorOnField(accessibleField.getName(), accessibleField.getDeclaringClass(), e);
            }
            final ProxyMethodHandler handler = new ProxyMethodHandler(beanManager.getContextId(),
                    new TargetBeanInstance(delegate), getBean());
            ((ProxyObject) instance).weld_setHandler(handler);
        }
    }

    @Override
    public void initializeAfterBeanDiscovery(EnhancedAnnotatedType<T> annotatedType) {
        // noop
    }

    @SuppressWarnings("unchecked")
    private void checkAbstractMethods(EnhancedAnnotatedType<T> type) {

        if (!type.isAbstract()) {
            return;
        }

        Set<Type> decoratedTypes = null;
        Bean<?> bean = getBean();

        if (bean != null && (bean instanceof DecoratorImpl)) {
            decoratedTypes = ((DecoratorImpl<T>) bean).getDecoratedTypes();
        }
        Decorators.checkAbstractMethods(decoratedTypes, type, beanManager);
    }
}
