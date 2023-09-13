/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.ejb;

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.ThreadLocalStack.ThreadLocalStackReference;
import org.jboss.weld.injection.producer.DefaultInjector;
import org.jboss.weld.injection.producer.Injector;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * {@link Injector} implementation which prepares {@link DynamicInjectionPoint} to be injected into stateless session beans or
 * singleton session beans.
 *
 * @see DefaultInjector
 * @see https://issues.jboss.org/browse/WELD-1177
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
class DynamicInjectionPointInjector<T> extends DefaultInjector<T> {

    private final CurrentInjectionPoint currentInjectionPoint;
    private boolean pushDynamicInjectionPoints;

    DynamicInjectionPointInjector(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        super(type, bean, beanManager);
        this.currentInjectionPoint = beanManager.getServices().get(CurrentInjectionPoint.class);
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx, BeanManagerImpl manager, SlimAnnotatedType<T> type,
            InjectionTarget<T> injectionTarget) {
        ThreadLocalStackReference<InjectionPoint> stack = null;
        if (pushDynamicInjectionPoints) {
            stack = currentInjectionPoint.push(new DynamicInjectionPoint(manager));
        }
        try {
            super.inject(instance, ctx, manager, type, injectionTarget);
        } finally {
            if (pushDynamicInjectionPoints) {
                stack.pop();
            }
        }
    }

    @Override
    public void registerInjectionPoints(Set<InjectionPoint> injectionPoints) {
        super.registerInjectionPoints(injectionPoints);
        pushDynamicInjectionPoints = hasInjectionPointMetadata(injectionPoints);
    }

    private boolean hasInjectionPointMetadata(Set<InjectionPoint> injectionPoints) {
        for (InjectionPoint injectionPoint : injectionPoints) {
            if (injectionPoint.getType() == InjectionPoint.class) {
                return true;
            }
        }
        return false;
    }
}
