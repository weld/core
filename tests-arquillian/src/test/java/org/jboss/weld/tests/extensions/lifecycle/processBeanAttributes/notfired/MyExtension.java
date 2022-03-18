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

package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.notfired;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;

public class MyExtension implements Extension {

   public static final String PROGRAMMATICALLY_ADDED_BEAN_NAME = "programmaticallyAdded";

    static final Set<Type> observedTypes = new HashSet<Type>();
    static final Set<String> observedNames = new HashSet<String>();

    public void addBean(@Observes AfterBeanDiscovery abd) {
        abd.addBean(new Bean<Foo>() {
            @Override
            public Class<?> getBeanClass() {
                return Foo.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return Collections.emptySet();
            }

            @Override
            public Set<Type> getTypes() {
                return new HashSet<Type>(Collections.singletonList(getBeanClass()));
            }

            @Override
            public Set<Annotation> getQualifiers() {
                return Collections.emptySet();
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return Dependent.class;
            }

            @Override
            public String getName() {
                return PROGRAMMATICALLY_ADDED_BEAN_NAME;
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

            @Override
            public Foo create(CreationalContext<Foo> creationalContext) {
                return null;
            }

            @Override
            public void destroy(Foo instance, CreationalContext<Foo> creationalContext) {
            }
        });
    }

    public void processBeanAttributes(@Observes ProcessBeanAttributes<?> event) {
        observedTypes.addAll(event.getBeanAttributes().getTypes());
        observedNames.add(event.getBeanAttributes().getName());
    }
}
