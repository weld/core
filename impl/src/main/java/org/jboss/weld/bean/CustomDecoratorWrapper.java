/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.bean;

import java.lang.reflect.Method;

import jakarta.enterprise.inject.spi.Decorator;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.runtime.InvokableAnnotatedMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.reflection.Reflections;

/**
 * A wrapper for a decorated instance. Allows to enhance custom decorators with metadata
 * about the WeldClass at deployment time.
 *
 * @author Marius Bogoevici
 */
public class CustomDecoratorWrapper<T> extends ForwardingDecorator<T> implements WeldDecorator<T> {
    private final Decorator<T> delegate;
    private final EnhancedAnnotatedType<T> weldClass;

    private final DecoratedMethods decoratedMethods;

    public static <T> CustomDecoratorWrapper<T> of(Decorator<T> delegate, BeanManagerImpl beanManager) {
        return new CustomDecoratorWrapper<T>(delegate, beanManager);
    }

    private CustomDecoratorWrapper(Decorator<T> delegate, BeanManagerImpl beanManager) {
        this.delegate = delegate;
        this.weldClass = beanManager.getServices().get(ClassTransformer.class)
                .getEnhancedAnnotatedType(Reflections.<Class<T>> cast(delegate.getBeanClass()), beanManager.getId());
        this.decoratedMethods = new DecoratedMethods(beanManager, this);
    }

    @Override
    public Decorator<T> delegate() {
        return delegate;
    }

    @Override
    public EnhancedAnnotatedType<?> getEnhancedAnnotated() {
        return weldClass;
    }

    @Override
    public InvokableAnnotatedMethod<?> getDecoratorMethod(Method method) {
        return decoratedMethods.getDecoratedMethod(method);
    }
}
