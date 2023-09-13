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

import java.lang.reflect.Member;

import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Reflections;

/**
 * An implementation of {@link WeldInjectionPointAttributes} that infers the attributes by reading
 * {@link EnhancedAnnotatedParameter}.
 *
 * @author Jozef Hartinger
 *
 */
public class InferringParameterInjectionPointAttributes<T, X> extends AbstractInferringInjectionPointAttributes<T, Object>
        implements ParameterInjectionPointAttributes<T, X> {

    private static final long serialVersionUID = 1237037554422642608L;

    public static <T, X> InferringParameterInjectionPointAttributes<T, X> of(EnhancedAnnotatedParameter<T, X> parameter,
            Bean<?> bean, Class<?> declaringComponentClass, BeanManagerImpl manager) {
        return new InferringParameterInjectionPointAttributes<T, X>(parameter, bean, declaringComponentClass, manager);
    }

    private final AnnotatedParameter<X> parameter;

    protected InferringParameterInjectionPointAttributes(EnhancedAnnotatedParameter<T, X> parameter, Bean<?> bean,
            Class<?> declaringComponentClass, BeanManagerImpl manager) {
        super(parameter, manager.getContextId(), bean,
                SharedObjectCache.instance(manager).getSharedSet(parameter.getQualifiers()), declaringComponentClass);
        this.parameter = parameter.slim();
    }

    @Override
    public Member getMember() {
        return getAnnotated().getDeclaringCallable().getJavaMember();
    }

    @Override
    public AnnotatedParameter<X> getAnnotated() {
        return parameter;
    }

    @Override
    public int hashCode() {
        return getAnnotated().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InferringParameterInjectionPointAttributes<?, ?>) {
            AnnotatedParameter<?> parameter = Reflections.<InferringParameterInjectionPointAttributes<?, ?>> cast(obj)
                    .getAnnotated();
            return AnnotatedTypes.compareAnnotatedParameters(getAnnotated(), parameter);
        }
        return false;
    }
}
