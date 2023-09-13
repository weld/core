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
package org.jboss.weld.injection.producer;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

/**
 * Creates a new Java object by calling its class constructor. This class is thread-safe.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class DefaultInstantiator<T> extends AbstractInstantiator<T> {

    private final ConstructorInjectionPoint<T> constructor;

    public DefaultInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl manager) {
        EnhancedAnnotatedConstructor<T> constructor = Beans.getBeanConstructor(type);
        this.constructor = InjectionPointFactory.instance().createConstructorInjectionPoint(bean, type.getJavaClass(),
                constructor, manager);
    }

    @Override
    public ConstructorInjectionPoint<T> getConstructorInjectionPoint() {
        return constructor;
    }

    @Override
    public Constructor<T> getConstructor() {
        if (constructor == null) {
            return null;
        }
        return constructor.getAnnotated().getJavaMember();
    }

    public List<ParameterInjectionPoint<?, T>> getParameterInjectionPoints() {
        if (constructor == null) {
            return Collections.emptyList();
        }
        return constructor.getParameterInjectionPoints();
    }

    @Override
    public String toString() {
        return "SimpleInstantiator [constructor=" + constructor.getMember() + "]";
    }

    @Override
    public boolean hasInterceptorSupport() {
        return false;
    }

    @Override
    public boolean hasDecoratorSupport() {
        return false;
    }
}
