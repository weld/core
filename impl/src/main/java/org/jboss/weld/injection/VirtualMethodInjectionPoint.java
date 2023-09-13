/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection;

import static org.jboss.weld.util.reflection.Reflections.isPackagePrivate;
import static org.jboss.weld.util.reflection.Reflections.isPrivate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableMap;

/**
 * {@link MethodInjectionPoint} that invokes virtual methods.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 * @param <X>
 */
class VirtualMethodInjectionPoint<T, X> extends StaticMethodInjectionPoint<T, X> {

    private volatile Map<Class<?>, Method> methods;

    VirtualMethodInjectionPoint(MethodInjectionPointType methodInjectionPointType, EnhancedAnnotatedMethod<T, X> enhancedMethod,
            Bean<?> declaringBean,
            Class<?> declaringComponentClass, Set<Class<? extends Annotation>> specialParameterMarkers,
            InjectionPointFactory factory,
            BeanManagerImpl manager) {
        super(methodInjectionPointType, enhancedMethod, declaringBean, declaringComponentClass, specialParameterMarkers,
                factory, manager);
        this.methods = Collections.<Class<?>, Method> singletonMap(getAnnotated().getJavaMember().getDeclaringClass(),
                accessibleMethod);
    }

    @Override
    protected Method getMethod(Object receiver) throws NoSuchMethodException {
        final Map<Class<?>, Method> methods = this.methods;
        Method method = this.methods.get(receiver.getClass());
        if (method == null) {
            // the same method may be written to the map twice, but that is ok
            // lookupMethod is very slow
            Method delegate = getAnnotated().getJavaMember();
            if ((hasDecorators() || MethodInjectionPointType.INITIALIZER.equals(type))
                    && (isPrivate(delegate) || isPackagePrivate(delegate.getModifiers())
                            && !Objects.equals(delegate.getDeclaringClass().getPackage(), receiver.getClass().getPackage()))) {
                // Initializer methods and decorated beans - overriding does not apply to private methods and package-private methods where the subclass is in a different package
                method = accessibleMethod;
            } else {
                method = SecurityActions.lookupMethod(receiver.getClass(), delegate.getName(), delegate.getParameterTypes());
                SecurityActions.ensureAccessible(method);
            }
            final Map<Class<?>, Method> newMethods = ImmutableMap.<Class<?>, Method> builder().putAll(methods)
                    .put(receiver.getClass(), method).build();
            this.methods = newMethods;
        }
        return method;
    }

    private boolean hasDecorators() {
        if (getBean() instanceof AbstractClassBean) {
            return ((AbstractClassBean<?>) getBean()).hasDecorators();
        }
        if (getBean() instanceof AbstractProducerBean) {
            return ((AbstractProducerBean<?, ?, ?>) getBean()).getDeclaringBean().hasDecorators();
        }
        return false;
    }
}
