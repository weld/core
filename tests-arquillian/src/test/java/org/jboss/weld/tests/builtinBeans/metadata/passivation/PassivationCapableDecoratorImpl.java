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
package org.jboss.weld.tests.builtinBeans.metadata.passivation;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;

/**
 * An implementation of {@link Decorator} which delegates to {@link InjectionTarget} and {@link BeanAttributes}. The bean is
 * passivation capable however the {@link PassivationCapableDecoratorImpl} class is intentionally not {@link Serializable} (it
 * does not have to).
 *
 * The container is required to wrap this non-serializable instance with a serialization proxy in order to make the bean a
 * passivation capable dependency.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class PassivationCapableDecoratorImpl<T> extends PassivationCapableBeanImpl<T> implements Decorator<T> {

    private final Type delegateType;
    private final Set<Annotation> delegateQualifiers;
    private final Set<Type> decoratedTypes;

    public PassivationCapableDecoratorImpl(Class<?> beanClass, BeanAttributes<T> attributes, Type delegateType,
            Set<Annotation> delegateQualifiers, Set<Type> decoratedTypes, InjectionTargetFactory<T> factory) {
        super(beanClass, attributes, factory);
        this.delegateType = delegateType;
        this.delegateQualifiers = Collections.unmodifiableSet(delegateQualifiers);
        this.decoratedTypes = Collections.unmodifiableSet(decoratedTypes);
    }

    @Override
    public Type getDelegateType() {
        return delegateType;
    }

    @Override
    public Set<Annotation> getDelegateQualifiers() {
        return delegateQualifiers;
    }

    @Override
    public Set<Type> getDecoratedTypes() {
        return decoratedTypes;
    }

}
