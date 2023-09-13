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

package org.jboss.weld.resolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.Interceptor;

import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class TypeSafeInterceptorResolver
        extends TypeSafeResolver<InterceptorResolvable, Interceptor<?>, List<Interceptor<?>>, List<Interceptor<?>>> {

    private final BeanManagerImpl manager;

    public TypeSafeInterceptorResolver(BeanManagerImpl manager, Iterable<Interceptor<?>> interceptors) {
        super(interceptors, manager.getServices().get(WeldConfiguration.class));
        this.manager = manager;
    }

    @Override
    protected boolean matches(InterceptorResolvable resolvable, Interceptor<?> bean) {
        return bean.intercepts(resolvable.getInterceptionType())
                && Beans.containsAllInterceptionBindings(bean.getInterceptorBindings(), resolvable.getQualifiers(),
                        getManager())
                && manager.getEnabled().isInterceptorEnabled(bean.getBeanClass());
    }

    @Override
    protected List<Interceptor<?>> sortResult(Set<Interceptor<?>> matchedInterceptors) {
        List<Interceptor<?>> sortedInterceptors = new ArrayList<Interceptor<?>>(matchedInterceptors);
        Collections.sort(sortedInterceptors, manager.getEnabled().getInterceptorComparator());
        return sortedInterceptors;
    }

    @Override
    protected Set<Interceptor<?>> filterResult(Set<Interceptor<?>> matched) {
        return matched;
    }

    public BeanManagerImpl getManager() {
        return manager;
    }
}
