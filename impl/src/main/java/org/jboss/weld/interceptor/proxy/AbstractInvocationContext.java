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
package org.jboss.weld.interceptor.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.interceptor.WeldInvocationContext;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.Primitives;
import org.jboss.weld.util.collections.ImmutableSet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

abstract class AbstractInvocationContext implements WeldInvocationContext {

    protected Map<String, Object> contextData;
    protected final Method method;
    protected Object[] parameters;
    protected final Object target;
    protected final Object timer;
    protected final Constructor<?> constructor;
    protected final Set<Annotation> interceptorBindings;
    protected final Method proceed;

    private static final Map<Class<?>, Set<Class<?>>> WIDENING_TABLE;

    static {
        Map<Class<?>, Set<Class<?>>> wideningTable = new HashMap<Class<?>, Set<Class<?>>>();
        wideningTable.put(byte.class,
                ImmutableSet.<Class<?>> of(short.class, int.class, long.class, float.class, double.class));
        wideningTable.put(short.class, ImmutableSet.<Class<?>> of(int.class, long.class, float.class, double.class));
        wideningTable.put(char.class, ImmutableSet.<Class<?>> of(int.class, long.class, float.class, double.class));
        wideningTable.put(int.class, ImmutableSet.<Class<?>> of(long.class, float.class, double.class));
        wideningTable.put(long.class, ImmutableSet.<Class<?>> of(float.class, double.class));
        wideningTable.put(float.class, Collections.<Class<?>> singleton(double.class));
        WIDENING_TABLE = Collections.unmodifiableMap(wideningTable);
    }

    protected AbstractInvocationContext(Object target, Method method, Method proceed, Object[] parameters,
            Map<String, Object> contextData, Set<Annotation> interceptorBindings) {
        this(target, method, proceed, null, parameters, null, contextData, interceptorBindings);
    }

    protected AbstractInvocationContext(Object target, Method method, Method proceed, Constructor<?> constructor,
            Object[] parameters, Object timer, Map<String, Object> contextData, Set<Annotation> interceptorBindings) {
        this.target = target;
        this.method = method;
        this.proceed = proceed;
        this.constructor = constructor;
        this.parameters = parameters;
        this.timer = timer;
        this.contextData = contextData;
        this.interceptorBindings = interceptorBindings != null ? interceptorBindings : Collections.emptySet();
    }

    @Override
    public Map<String, Object> getContextData() {
        if (contextData == null) {
            contextData = newContextData(interceptorBindings);
        }
        return contextData;
    }

    protected static Map<String, Object> newContextData(Set<Annotation> interceptorBindings) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(WeldInvocationContext.INTERCEPTOR_BINDINGS_KEY, interceptorBindings);
        return result;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Object[] getParameters() {
        if (this.method != null || this.constructor != null) {
            return parameters;
        } else {
            throw new IllegalStateException("Illegal invocation to getParameters() during lifecycle invocation");
        }
    }

    @Override
    public Object getTarget() {
        return target;
    }

    /**
     * Checks that the targetClass is widening the argument class
     *
     * @param argumentClass
     * @param targetClass
     * @return
     */
    private static boolean isWideningPrimitive(Class<?> argumentClass, Class<?> targetClass) {
        return WIDENING_TABLE.containsKey(argumentClass) && WIDENING_TABLE.get(argumentClass).contains(targetClass);
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public void setParameters(Object[] params) {
        if (this.method != null || this.constructor != null) {
            // there is no requirement to do anything if params is null
            // but this is theoretically possible only if the target method has no arguments
            int newParametersCount = params == null ? 0 : params.length;
            Class<?>[] parameterTypes = null;
            if (method != null) {
                parameterTypes = method.getParameterTypes();
            } else {
                parameterTypes = constructor.getParameterTypes();
            }
            if (parameterTypes.length != newParametersCount) {
                throw new IllegalArgumentException(
                        "Wrong number of parameters: method has " + parameterTypes.length + ", attempting to set "
                                + newParametersCount + (params != null ? "" : " (argument was null)"));
            }
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    Class<?> methodParameterClass = parameterTypes[i];
                    if (params[i] != null) {
                        // identity ok
                        Class<?> newArgumentClass = params[i].getClass();
                        if (newArgumentClass.equals(methodParameterClass)) {
                            break;
                        }
                        if (newArgumentClass.isPrimitive()) {
                            // argument is primitive - never actually a case for interceptors
                            if (methodParameterClass.isPrimitive()) {
                                // widening primitive
                                if (!isWideningPrimitive(newArgumentClass, methodParameterClass)) {
                                    throwIAE(i, methodParameterClass, newArgumentClass);
                                }
                            } else {
                                // boxing+widening reference
                                Class<?> boxedArgumentClass = Primitives.wrap(newArgumentClass);
                                if (!methodParameterClass.isAssignableFrom(boxedArgumentClass)) {
                                    throwIAE(i, methodParameterClass, newArgumentClass);
                                }
                            }
                        } else {
                            // argument is non-primitive
                            if (methodParameterClass.isPrimitive()) {
                                // unboxing+widening primitive
                                Class<?> unboxedClass = Primitives.unwrap(newArgumentClass);

                                if (!unboxedClass.equals(methodParameterClass)
                                        && !isWideningPrimitive(unboxedClass, methodParameterClass)) {
                                    throwIAE(i, methodParameterClass, newArgumentClass);
                                }
                            } else {
                                // widening reference
                                if (!methodParameterClass.isAssignableFrom(newArgumentClass)) {
                                    throwIAE(i, methodParameterClass, newArgumentClass);
                                }
                            }
                        }
                    } else {
                        // null is never acceptable on a primitive type
                        if (parameterTypes[i].isPrimitive()) {
                            throw new IllegalArgumentException(
                                    "Trying to set a null value on a " + parameterTypes[i].getName());
                        }
                    }
                }
                this.parameters = params;
            }
        } else {
            throw new IllegalStateException("Illegal invocation to setParameters() during lifecycle invocation");
        }
    }

    private void throwIAE(int i, Class<?> methodParameterClass, Class<?> newArgumentClass) {
        throw new IllegalArgumentException(
                "Incompatible parameter type on position: " + i + " :" + newArgumentClass + " (expected type was "
                        + methodParameterClass.getName() + ")");
    }

    @Override
    public Object getTimer() {
        return timer;
    }

    @Override
    public Constructor<?> getConstructor() {
        return constructor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Annotation> Set<T> getInterceptorBindingsByType(Class<T> annotationType) {
        Preconditions.checkArgumentNotNull(annotationType, "annotationType");
        return interceptorBindings.stream()
                .filter((annotation) -> annotation.annotationType().equals(annotationType))
                .map(annotation -> (T) annotation)
                .collect(ImmutableSet.collector());
    }

    @Override
    public Set<Annotation> getInterceptorBindings() {
        return interceptorBindings;
    }

    protected Method getProceed() {
        return proceed;
    }
}
