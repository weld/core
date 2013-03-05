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
package org.jboss.weld.injection.producer;

import static org.jboss.weld.util.collections.WeldCollections.immutableSet;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.ResourceInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.ArraySet;

/**
 * {@link Injector} that adds support for resource field injection.
 *
 * @see DefaultInjector
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class ResourceInjector<T> extends DefaultInjector<T> {

    private final Set<ResourceInjectionPoint<?, ?>> resourceInjectionPoints;

    protected ResourceInjector(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        super(type, bean, beanManager);
        Set<ResourceInjectionPoint<?, ?>> resourceInjectionPoints = new ArraySet<ResourceInjectionPoint<?, ?>>();
        resourceInjectionPoints.addAll(InjectionPointFactory.silentInstance().getEjbInjectionPoints(bean, type, beanManager));
        resourceInjectionPoints.addAll(InjectionPointFactory.silentInstance().getPersistenceContextInjectionPoints(bean, type, beanManager));
        resourceInjectionPoints.addAll(InjectionPointFactory.silentInstance().getPersistenceUnitInjectionPoints(bean, type, beanManager));
        resourceInjectionPoints.addAll(InjectionPointFactory.silentInstance().getResourceInjectionPoints(bean, type, beanManager));
        resourceInjectionPoints.addAll(InjectionPointFactory.silentInstance().getWebServiceRefInjectionPoints(bean, type, beanManager));
        this.resourceInjectionPoints = immutableSet(resourceInjectionPoints);
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx, BeanManagerImpl manager) {
        Beans.injectEEFields(resourceInjectionPoints, instance, ctx);
        super.inject(instance, ctx, manager);
    }
}
