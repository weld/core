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

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.injection.ResourceInjection;
import org.jboss.weld.injection.ResourceInjectionFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.ImmutableList;

/**
 * {@link Injector} that adds support for resource field and setter injection.
 *
 * @see DefaultInjector
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 *
 * @param <T>
 */
public class ResourceInjector<T> extends DefaultInjector<T> {

    public static <T> ResourceInjector<T> of(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        return new ResourceInjector<T>(type, bean, beanManager);
    }

    /**
     * Holds sets of resource injection points per class in type hierarchy
     */
    private List<Set<ResourceInjection<?>>> resourceInjectionsHierarchy;

    protected ResourceInjector(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        super(type, bean, beanManager);
        final ResourceInjectionFactory factory = beanManager.getServices().get(ResourceInjectionFactory.class);
        this.resourceInjectionsHierarchy = ImmutableList.copyOf(factory.getResourceInjections(bean, type, beanManager));
    }

    @Override
    public void inject(final T instance, final CreationalContext<T> ctx, final BeanManagerImpl manager,
            final SlimAnnotatedType<T> type, final InjectionTarget<T> injectionTarget) {
        new InjectionContextImpl<T>(manager, injectionTarget, type, instance) {
            @Override
            public void proceed() {
                // Java EE component environment resource dependencies are injected first
                Beans.injectEEFields(resourceInjectionsHierarchy, instance, ctx);
                Beans.injectFieldsAndInitializers(instance, ctx, manager, getInjectableFields(), getInitializerMethods());
            }
        }.run();
    }
}
