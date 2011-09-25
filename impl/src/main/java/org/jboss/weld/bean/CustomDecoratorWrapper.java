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

import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Decorators;
import org.jboss.weld.util.reflection.Reflections;

import javax.enterprise.inject.spi.Decorator;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * A wrapper for a decorated instance. Allows to enhance custom decorators with metadata
 * about the WeldClass at deployment time.
 *
 * @author Marius Bogoevici
 */
public class CustomDecoratorWrapper<T> extends ForwardingDecorator<T> implements WeldDecorator<T> {
    private Decorator<T> delegate;
    private WeldClass<T> weldClass;

    private Map<MethodSignature, WeldMethod<?, ?>> decoratorMethods;

    public static <T> CustomDecoratorWrapper<T> of(Decorator<T> delegate, BeanManagerImpl beanManager) {
        return new CustomDecoratorWrapper<T>(delegate, beanManager);
    }

    private CustomDecoratorWrapper(Decorator<T> delegate, BeanManagerImpl beanManager) {
        this.delegate = delegate;
        this.weldClass = beanManager.getServices().get(ClassTransformer.class).loadClass(Reflections.<Class<T>>cast(delegate.getBeanClass()));
        this.decoratorMethods = Decorators.getDecoratorMethods(beanManager, delegate.getDecoratedTypes(), this.weldClass);
    }

    @Override
    protected Decorator<T> delegate() {
        return delegate;
    }

    public WeldClass<?> getWeldAnnotated() {
        return weldClass;
    }

    public WeldMethod<?, ?> getDecoratorMethod(Method method) {
        return Decorators.findDecoratorMethod(this, decoratorMethods, method);
    }
}
