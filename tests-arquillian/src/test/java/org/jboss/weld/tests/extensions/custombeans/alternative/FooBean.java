/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.extensions.custombeans.alternative;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Prioritized;

public class FooBean implements Bean<Foo>, Prioritized {
    @Override
    public Class<?> getBeanClass() {
        return Foo.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Foo create(CreationalContext<Foo> creationalContext) {
        return new Foo(FooBean.class.getSimpleName());
    }

    @Override
    public void destroy(Foo instance, CreationalContext<Foo> creationalContext) {
    }

    private <T> Set<T> immutableSet(T... items) {
        Set<T> set = new HashSet<T>();
        Collections.addAll(set, items);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Set<Type> getTypes() {
        return immutableSet(Object.class, Foo.class);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return immutableSet(Default.Literal.INSTANCE);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.EMPTY_SET;
    }

    @Override
    public boolean isAlternative() {
        return true;
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
