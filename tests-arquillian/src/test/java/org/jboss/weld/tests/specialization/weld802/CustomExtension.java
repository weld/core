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
package org.jboss.weld.tests.specialization.weld802;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.annotated.ForwardingWeldClass;

/**
 * @author Ales Justin
 */
public class CustomExtension implements Extension {
    public void registerBeans(@Observes BeforeBeanDiscovery event, final BeanManager manager) {
        final EnhancedAnnotatedType<Foo> foo = getEnhancedAnnotatedType(manager, Foo.class);
        final EnhancedAnnotatedType<Bar> bar = new ForwardingWeldClass<Bar>() {
            @Override
            protected EnhancedAnnotatedType<Bar> delegate() {
                return getEnhancedAnnotatedType(manager, Bar.class);
            }

            @Override
            public EnhancedAnnotatedType<? super Bar> getEnhancedSuperclass() {
                return foo;
            }
        };
        event.addAnnotatedType(foo, Foo.class.getSimpleName());
        event.addAnnotatedType(bar, Bar.class.getSimpleName());
    }

    protected <T> EnhancedAnnotatedType<T> getEnhancedAnnotatedType(BeanManager manager, Class<T> javaClass) {
        if (manager instanceof BeanManagerProxy) {
            BeanManagerProxy proxy = (BeanManagerProxy) manager;
            manager = proxy.delegate();
        }
        if (manager instanceof BeanManagerImpl) {
            BeanManagerImpl bmi = (BeanManagerImpl) manager;
            return ((BeanManagerImpl) manager).createEnhancedAnnotatedType(javaClass);
        } else {
            throw new IllegalStateException();
        }
    }
}
