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
package org.jboss.weld.enums;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.injection.ResourceInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.injection.producer.DefaultInjectionTarget;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.InjectionPoints;

/**
 * An {@link InjectionTarget} implementation capable of injecting Java enums.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 *
 * @param <T> enum type
 */

public class EnumInjectionTarget<T extends Enum<?>> extends DefaultInjectionTarget<T> {
    public static <T extends Enum<?>> EnumInjectionTarget<T> of(EnhancedAnnotatedType<T> clazz, BeanManagerImpl manager) {
        return new EnumInjectionTarget<T>(clazz, manager);
    }

    public EnumInjectionTarget(EnhancedAnnotatedType<T> type, BeanManagerImpl beanManager) {
        super(type, null, beanManager);
    }

    @Override
    protected Instantiator<T> initInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager, Set<InjectionPoint> injectionPoints) {
        return null; // we never create an enum instance
    }

    @Override
    protected void checkType(EnhancedAnnotatedType<T> type) {
        // noop
    }

    @Override
    public void dispose(T instance) {
        for (ResourceInjectionPoint<?, ?> ip : getResourceInjectionPoints()) {
            ip.disinject(instance);
        }
        for (InjectionPoint ip : getInjectionPoints()) {
            if (ip.getAnnotated() instanceof AnnotatedField<?>) {
                disinject(InjectionPoints.getWeldInjectionPoint(ip), instance);
            }
        }
    }

    /**
     * Sets injected values back to null.
     */
    protected void disinject(Set<? extends WeldInjectionPoint<?, ?>> injectionPoints, T instance) {
        for (WeldInjectionPoint<?, ?> ip : injectionPoints) {
            disinject(ip, instance);
        }
    }

    protected void disinject(WeldInjectionPoint<?, ?> wip, T instance) {
        wip.inject(instance, null);
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void postConstruct(T instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void preDestroy(T instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "EnumInjectionTarget for " + getAnnotated();
    }
}
