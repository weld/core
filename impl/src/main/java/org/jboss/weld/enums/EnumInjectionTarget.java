/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.enums;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
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
        disinject(getEjbInjectionPoints(), instance);
        disinject(getPersistenceContextInjectionPoints(), instance);
        disinject(getPersistenceUnitInjectionPoints(), instance);
        disinject(getResourceInjectionPoints(), instance);
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
