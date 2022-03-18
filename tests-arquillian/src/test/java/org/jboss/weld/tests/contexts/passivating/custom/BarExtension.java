/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.passivating.custom;

import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.util.bean.ForwardingBeanAttributes;

public class BarExtension implements Extension {

    void registerBar(@Observes AfterBeanDiscovery event, BeanManager manager) {
        AnnotatedType<Bar> annotatedType = manager.createAnnotatedType(Bar.class);
        final BeanAttributes<Bar> attributes = manager.createBeanAttributes(annotatedType);
        Bean<Bar> bean = new AbstractPassivationCapableBean<Bar>() {

            @Override
            public Class<?> getBeanClass() {
                return Bar.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return Collections.emptySet();
            }

            @Override
            public Bar create(CreationalContext<Bar> creationalContext) {
                return new Bar();
            }

            @Override
            public void destroy(Bar instance, CreationalContext<Bar> creationalContext) {
                creationalContext.release();
            }

            @Override
            public String getId() {
                return getBeanClass().getCanonicalName();
            }

            @Override
            protected BeanAttributes<Bar> attributes() {
                return attributes;
            }
        };
        event.addBean(bean);
    }

    private static abstract class AbstractPassivationCapableBean<T> extends ForwardingBeanAttributes<T> implements Bean<T>,
            PassivationCapable {
    }
}
