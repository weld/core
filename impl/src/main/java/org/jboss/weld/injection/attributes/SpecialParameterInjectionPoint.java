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
package org.jboss.weld.injection.attributes;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Represents a method/constructor parameter, which is not an injection point. This can be either a disposed or event parameter.
 *
 * @author Jozef Hartinger
 *
 */
public class SpecialParameterInjectionPoint<T, X> extends ForwardingInjectionPointAttributes<T, Object>
        implements ParameterInjectionPoint<T, X> {

    public static <T, X> ParameterInjectionPoint<T, X> of(EnhancedAnnotatedParameter<T, X> parameter, Bean<?> bean,
            Class<?> declaringComponentClass, BeanManagerImpl manager) {
        return new SpecialParameterInjectionPoint<T, X>(parameter, bean, declaringComponentClass, manager);
    }

    private final ParameterInjectionPointAttributes<T, X> attributes;

    protected SpecialParameterInjectionPoint(EnhancedAnnotatedParameter<T, X> parameter, Bean<?> bean,
            Class<?> declaringComponentClass, BeanManagerImpl manager) {
        this.attributes = InferringParameterInjectionPointAttributes.of(parameter, bean, declaringComponentClass, manager);
    }

    @Override
    public AnnotatedParameter<X> getAnnotated() {
        return attributes.getAnnotated();
    }

    @Override
    public T getValueToInject(BeanManagerImpl manager, CreationalContext<?> creationalContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ParameterInjectionPointAttributes<T, X> delegate() {
        return attributes;
    }
}
