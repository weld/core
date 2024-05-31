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
package org.jboss.weld.annotated.runtime;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.util.annotated.ForwardingAnnotatedMethod;
import org.jboss.weld.util.collections.WeldCollections;
import org.jboss.weld.util.reflection.Reflections;

/**
 * An implementation of {@link AnnotatedMethod} used at runtime for invoking Java methods.
 *
 * @author Jozef Hartinger
 */
public class InvokableAnnotatedMethod<T> extends ForwardingAnnotatedMethod<T> {

    public static <T> InvokableAnnotatedMethod<T> of(AnnotatedMethod<T> delegate) {
        return new InvokableAnnotatedMethod<T>(delegate);
    }

    private final AnnotatedMethod<T> annotatedMethod;
    private volatile Map<Class<?>, Method> methods;

    public InvokableAnnotatedMethod(AnnotatedMethod<T> annotatedMethod) {
        this.annotatedMethod = annotatedMethod;
        Method method = annotatedMethod.getJavaMember();
        this.methods = Collections.<Class<?>, Method> singletonMap(method.getDeclaringClass(), method);
        if (method != null && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    /**
     * Invokes the method
     *
     * @param instance The instance to invoke
     * @param parameters The method parameters
     * @return A reference to the instance
     */
    public <X> X invoke(Object instance, Object... parameters)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return cast(annotatedMethod.getJavaMember().invoke(instance, parameters));
    }

    /**
     * Invokes the method on the class of the passed instance, not the declaring
     * class. Useful with proxies
     *
     * @param instance The instance to invoke
     * @param parameters The method parameters
     * @return A reference to the instance
     */
    public <X> X invokeOnInstance(Object instance, Object... parameters) throws IllegalArgumentException, SecurityException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Map<Class<?>, Method> methods = this.methods;
        Method method = methods.get(instance.getClass());
        if (method == null) {
            // the same method may be written to the map twice, but that is ok
            // lookupMethod is very slow
            Method delegate = annotatedMethod.getJavaMember();
            method = Reflections.lookupMethod(instance.getClass(), delegate.getName(), delegate.getParameterTypes());
            if (method != null && !method.canAccess(instance)) {
                method.setAccessible(true);
            }
            synchronized (this) {
                final Map<Class<?>, Method> newMethods = new HashMap<Class<?>, Method>(methods);
                newMethods.put(instance.getClass(), method);
                this.methods = WeldCollections.immutableMapView(newMethods);
            }
        }
        return cast(method.invoke(instance, parameters));
    }

    @Override
    public AnnotatedMethod<T> delegate() {
        return annotatedMethod;
    }
}
