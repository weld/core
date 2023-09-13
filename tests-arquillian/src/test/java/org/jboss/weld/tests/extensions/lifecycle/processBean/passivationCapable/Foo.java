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
package org.jboss.weld.tests.extensions.lifecycle.processBean.passivationCapable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;

public class Foo implements Bean<String>, PassivationCapable {

    private static final Set<Type> TYPES = new HashSet<Type>();

    private final static Set<Annotation> BINDING_TYPES = new HashSet<Annotation>();

    static {
        TYPES.add(String.class);
        TYPES.add(Object.class);
        BINDING_TYPES.add(Default.Literal.INSTANCE);
        BINDING_TYPES.add(Any.Literal.INSTANCE);
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String create(CreationalContext<String> creationalContext) {
        return "foo";
    }

    @Override
    public void destroy(String instance, CreationalContext<String> creationalContext) {
    }

    @Override
    public Set<Type> getTypes() {
        return Collections.unmodifiableSet(TYPES);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.unmodifiableSet(BINDING_TYPES);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return "foo";
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
    public Class<?> getBeanClass() {
        return String.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }
}
