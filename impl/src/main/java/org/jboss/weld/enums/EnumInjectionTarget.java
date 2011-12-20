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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.ArraySet;

/**
 * An {@link InjectionTarget} implementation capable of injecting Java enums.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 *
 * @param <T> enum type
 */
public class EnumInjectionTarget<T extends Enum<?>> implements InjectionTarget<T> {

    public static <T extends Enum<?>> EnumInjectionTarget<T> of(WeldClass<T> clazz, BeanManagerImpl manager) {
        return new EnumInjectionTarget<T>(clazz, manager);
    }

    // The injectable fields of each type in the type hierarchy, with the actual
    // type at the bottom
    private final List<Set<FieldInjectionPoint<?, ?>>> injectableFields;

    // The initializer methods of each type in the type hierarchy, with the
    // actual type at the bottom
    private final List<Set<MethodInjectionPoint<?, ?>>> initializerMethods;

    // The Java EE style injection points
    private final Set<WeldInjectionPoint<?, ?>> ejbInjectionPoints;
    private final Set<WeldInjectionPoint<?, ?>> persistenceContextInjectionPoints;
    private final Set<WeldInjectionPoint<?, ?>> persistenceUnitInjectionPoints;
    private final Set<WeldInjectionPoint<?, ?>> resourceInjectionPoints;

    private final ArraySet<WeldInjectionPoint<?, ?>> injectionPoints;
    private final ArraySet<WeldInjectionPoint<?, ?>> newInjectionPoints;

    private final WeldClass<T> weldClass;

    private final BeanManagerImpl manager;

    public EnumInjectionTarget(WeldClass<T> weldClass, BeanManagerImpl manager) {
        this.manager = manager;
        this.weldClass = weldClass;
        this.injectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
        this.newInjectionPoints = new ArraySet<WeldInjectionPoint<?, ?>>();
        this.injectableFields = Beans.getFieldInjectionPoints(null, weldClass, manager);
        this.ejbInjectionPoints = Beans.getEjbInjectionPoints(null, weldClass, manager);
        this.persistenceContextInjectionPoints = Beans.getPersistenceContextInjectionPoints(null, weldClass, manager);
        this.persistenceUnitInjectionPoints = Beans.getPersistenceUnitInjectionPoints(null, weldClass, manager);
        this.resourceInjectionPoints = Beans.getResourceInjectionPoints(null, weldClass, manager);
        this.initializerMethods = Beans.getInitializerMethods(null, weldClass, manager);
        addInjectionPoints(Beans.flattenInjectionPoints(injectableFields));
        addInjectionPoints(Beans.flattenParameterInjectionPoints(initializerMethods));
        injectionPoints.trimToSize();
        newInjectionPoints.trimToSize();
    }

    public void inject(T instance, CreationalContext<T> ctx) {
        Beans.injectEEFields(instance, manager, ejbInjectionPoints, persistenceContextInjectionPoints, persistenceUnitInjectionPoints, resourceInjectionPoints);
        Beans.injectFieldsAndInitializers(instance, ctx, manager, injectableFields, initializerMethods);
    }

    @Override
    public void dispose(T instance) {
        disinject(ejbInjectionPoints, instance);
        disinject(persistenceContextInjectionPoints, instance);
        disinject(persistenceUnitInjectionPoints, instance);
        disinject(resourceInjectionPoints, instance);
        for (Set<FieldInjectionPoint<?, ?>> fields : injectableFields) {
            disinject(fields, instance);
        }
    }

    /**
     * Sets injected values back to null.
     */
    protected void disinject(Set<? extends WeldInjectionPoint<?, ?>> injectionPoints, T instance) {
        for (WeldInjectionPoint<?, ?> ip : injectionPoints) {
            ip.inject(instance, null);
        }
    }

    protected <X extends WeldInjectionPoint<?, ?>> void addInjectionPoints(Iterable<X> injectionPoints) {
        for (X ip : injectionPoints) {
            addInjectionPoint(ip);
        }
    }

    protected void addInjectionPoint(WeldInjectionPoint<?, ?> ip) {
        if (ip.getQualifier(New.class) != null) {
            newInjectionPoints.add(ip);
        }
        injectionPoints.add(ip);
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.<InjectionPoint> unmodifiableSet(injectionPoints);
    }

    public Set<WeldInjectionPoint<?, ?>> getNewInjectionPoints() {
        return Collections.unmodifiableSet(newInjectionPoints);
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
        return "EnumInjectionTarget for " + weldClass;
    }
}
