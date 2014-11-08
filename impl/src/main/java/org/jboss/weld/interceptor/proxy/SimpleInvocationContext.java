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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.interceptor.InvocationContext;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Simple {@link InvocationContext} implementation whose {@link #proceed()} invokes the target method directly without calling any interceptors. If this is not
 * a method interception, a call to {@link #proceed()} always returns null.
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 * @author Jozef Hartinger
 */
public class SimpleInvocationContext implements InvocationContext {

    private final Map<String, Object> contextData;
    private final Method method;
    private final Method proceed;
    private Object[] parameters;
    private final Object target;
    private final Object timer;
    private final Constructor<?> constructor;

    private static final Map<Class<?>, Set<Class<?>>> WIDENING_TABLE;

    static {
        Map<Class<?>, Set<Class<?>>> wideningTable = new HashMap<Class<?>, Set<Class<?>>>();
        wideningTable.put(byte.class, ImmutableSet.<Class<?>> of(short.class, int.class, long.class, float.class, double.class));
        wideningTable.put(short.class, ImmutableSet.<Class<?>> of(int.class, long.class, float.class, double.class));
        wideningTable.put(char.class, ImmutableSet.<Class<?>> of(int.class, long.class, float.class, double.class));
        wideningTable.put(int.class, ImmutableSet.<Class<?>> of(long.class, float.class, double.class));
        wideningTable.put(long.class, ImmutableSet.<Class<?>> of(float.class, double.class));
        wideningTable.put(float.class, Collections.<Class<?>> singleton(double.class));
        WIDENING_TABLE = Collections.unmodifiableMap(wideningTable);

    }

    public SimpleInvocationContext(Object target, Method targetMethod, Method proceed, Object[] parameters) {
        this(target, targetMethod, proceed, null, parameters, null, new HashMap<String, Object>());
    }

    public SimpleInvocationContext(Constructor<?> constructor, Object[] parameters, Map<String, Object> contextData) {
        this(null, null, null, constructor, parameters, null, contextData);
    }

    private SimpleInvocationContext(Object target, Method method, Method proceed, Constructor<?> constructor, Object[] parameters, Object timer, Map<String, Object> contextData) {
        this.target = target;
        this.method = method;
        this.proceed = proceed;
        this.constructor = constructor;
        this.parameters = parameters;
        this.timer = timer;
        this.contextData = contextData;
    }

    @Override
    public Map<String, Object> getContextData() {
        return contextData;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    @SuppressWarnings("EI_EXPOSE_REP")
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

    @SuppressWarnings("EI_EXPOSE_REP")
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
                throw new IllegalArgumentException("Wrong number of parameters: method has " + parameterTypes.length + ", attempting to set "
                        + newParametersCount + (params != null ? "" : " (argument was null)"));
            }
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    Class<?> methodParameterClass = parameterTypes[i];
                    if (params[i] != null) {
                        // identity ok
                        Class<? extends Object> newArgumentClass = params[i].getClass();
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

                                if (!unboxedClass.equals(methodParameterClass) && !isWideningPrimitive(unboxedClass, methodParameterClass)) {
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
                            throw new IllegalArgumentException("Trying to set a null value on a " + parameterTypes[i].getName());
                        }
                    }
                }
                this.parameters = params;
            }
        } else {
            throw new IllegalStateException("Illegal invocation to setParameters() during lifecycle invocation");
        }
    }

    private void throwIAE(int i, Class<?> methodParameterClass, Class<? extends Object> newArgumentClass) {
        throw new IllegalArgumentException("Incompatible parameter type on position: " + i + " :" + newArgumentClass + " (expected type was "
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
    public Object proceed() throws Exception {
        if (proceed != null) {
            SecurityActions.ensureAccessible(proceed);
            return proceed.invoke(getTarget(), getParameters());
        } else {
            return null;
        }
    }
}