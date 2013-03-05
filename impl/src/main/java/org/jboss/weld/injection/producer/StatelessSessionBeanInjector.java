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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.DynamicInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * {@link Injector} implementation which prepares {@link DynamicInjectionPoint} to be injected into stateless session beans.
 *
 * @see DefaultInjector
 * @see https://issues.jboss.org/browse/WELD-1177
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class StatelessSessionBeanInjector<T> extends DefaultInjector<T> {

    private final CurrentInjectionPoint currentInjectionPoint;

    public StatelessSessionBeanInjector(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        super(type, bean, beanManager);
        this.currentInjectionPoint = beanManager.getServices().get(CurrentInjectionPoint.class);
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx, BeanManagerImpl manager) {
        currentInjectionPoint.push(new DynamicInjectionPoint(manager.getServices()));
        try {
            super.inject(instance, ctx, manager);
        } finally {
            currentInjectionPoint.pop();
        }
    }
}
