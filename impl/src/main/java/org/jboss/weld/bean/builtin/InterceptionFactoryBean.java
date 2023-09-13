/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.builtin;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InterceptionFactory;

import org.jboss.weld.injection.InterceptionFactoryImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

/**
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public class InterceptionFactoryBean extends AbstractStaticallyDecorableBuiltInBean<InterceptionFactory<?>> {

    private static final Set<Type> TYPES = ImmutableSet.<Type> of(InterceptionFactory.class, Object.class);

    public InterceptionFactoryBean(BeanManagerImpl beanManager) {
        super(beanManager, Reflections.<Class<InterceptionFactory<?>>> cast(InterceptionFactory.class));
    }

    @Override
    protected InterceptionFactory<?> newInstance(InjectionPoint ip,
            CreationalContext<InterceptionFactory<?>> creationalContext) {
        AnnotatedParameter<?> annotatedParameter = (AnnotatedParameter<?>) ip.getAnnotated();
        ParameterizedType parameterizedType = (ParameterizedType) annotatedParameter.getBaseType();
        AnnotatedType<?> annotatedType = beanManager
                .createAnnotatedType(Reflections.getRawType(parameterizedType.getActualTypeArguments()[0]));
        return InterceptionFactoryImpl.of(beanManager, creationalContext, annotatedType);
    }

    @Override
    public Set<Type> getTypes() {
        return TYPES;
    }

    @Override
    public String toString() {
        return "Implicit Bean [" + InterceptionFactory.class.getName() + "] with qualifiers [@Default]";
    }

}
