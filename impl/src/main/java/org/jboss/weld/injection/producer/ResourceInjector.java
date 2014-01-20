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

import static org.jboss.weld.util.collections.WeldCollections.immutableGuavaList;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.injection.ResourceInjection;
import org.jboss.weld.injection.ResourceInjectionFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

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

    /**
     * Holds sets of resource injection points per class in type hierarchy
     */
    private List<Set<ResourceInjection<?>>> resourceInjectionsHierarchy;

    protected ResourceInjector(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        super(type, bean, beanManager);
        this.resourceInjectionsHierarchy = immutableGuavaList(ResourceInjectionFactory.instance().getResourceInjections(bean, type,
                beanManager));
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx, BeanManagerImpl manager) {
        // Java EE component environment resource dependencies are injected first
        Beans.injectEEFields(resourceInjectionsHierarchy, instance, ctx);
        super.inject(instance, ctx, manager);
    }
}
