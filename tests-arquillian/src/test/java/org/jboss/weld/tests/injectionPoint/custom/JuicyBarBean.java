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
package org.jboss.weld.tests.injectionPoint.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;

public class JuicyBarBean implements Bean<Bar> {

    private final BeanManager beanManager;

    public JuicyBarBean(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public Bar create(CreationalContext<Bar> creationalContext) {
        InjectionPoint injectionPointMetadata = beanManager.createInstance().select(InjectionPoint.class).get();
        return new Bar(injectionPointMetadata);
    }

    @Override
    public void destroy(Bar instance, CreationalContext<Bar> creationalContext) {
        creationalContext.release();
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> immutableSet(T... items) {
        Set<T> set = new HashSet<T>();
        Collections.addAll(set, items);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Set<Type> getTypes() {
        return this.<Type> immutableSet(Object.class, Bar.class);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return this.<Annotation> immutableSet(Juicy.Literal.INSTANCE);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
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
        return JuicyBarBean.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

}
