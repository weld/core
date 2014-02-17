/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.interceptor.InvocationContext;

import org.jboss.weld.interceptor.spi.context.InterceptionChain;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class InterceptorInvocationContext implements InvocationContext {

    private final Map<String, Object> contextData;

    private final Method method;

    private Object[] parameters;

    private final Object target;

    private final InterceptionChain interceptionChain;

    private final Object timer;

    private final Constructor<?> constructor;

    private static final Map<Class<?>, Set<Class<?>>> WIDENING_TABLE;

    private static final Map<Class<?>, Class<?>> WRAPPER_CLASSES;
    private static final Map<Class<?>, Class<?>> REVERSE_WRAPPER_CLASSES;

    static {
        Map<Class<?>, Class<?>> wrapperClasses = new HashMap<Class<?>, Class<?>>();
        wrapperClasses.put(boolean.class, Boolean.class);
        wrapperClasses.put(byte.class, Byte.class);
        wrapperClasses.put(char.class, Character.class);
        wrapperClasses.put(short.class, Short.class);
        wrapperClasses.put(int.class, Integer.class);
        wrapperClasses.put(long.class, Long.class);
        wrapperClasses.put(float.class, Float.class);
        wrapperClasses.put(double.class, Double.class);

        WRAPPER_CLASSES = Collections.unmodifiableMap(wrapperClasses);

        Map<Class<?>, Class<?>> reverseWrapperClasses = new HashMap<Class<?>, Class<?>>();
        for (Map.Entry<Class<?>, Class<?>> classEntry : wrapperClasses.entrySet()) {
            reverseWrapperClasses.put(classEntry.getValue(), classEntry.getKey());
        }

        REVERSE_WRAPPER_CLASSES = Collections.unmodifiableMap(reverseWrapperClasses);

        Map<Class<?>, Set<Class<?>>> wideningTable = new HashMap<Class<?>, Set<Class<?>>>();
        wideningTable.put(byte.class, setOf(short.class, int.class, long.class, float.class, double.class));
        wideningTable.put(short.class, setOf(int.class, long.class, float.class, double.class));
        wideningTable.put(char.class, setOf(int.class, long.class, float.class, double.class));
        wideningTable.put(int.class, setOf(long.class, float.class, double.class));
        wideningTable.put(long.class, setOf(float.class, double.class));
        wideningTable.put(float.class, Collections.<Class<?>>singleton(double.class));
        WIDENING_TABLE = Collections.unmodifiableMap(wideningTable);

    }

    private static Set<Class<?>> setOf(Class<?>... classes) {
        return new HashSet<Class<?>>(Arrays.asList(classes));
    }

    public InterceptorInvocationContext(InterceptionChain interceptionChain, Object target, Method targetMethod, Object[] parameters) {
        this(interceptionChain, target, targetMethod, null, parameters, null);
    }

    public InterceptorInvocationContext(InterceptionChain interceptionChain, Object target, Method targetMethod, Object timer) {
        this(interceptionChain, target, targetMethod, null, null, timer);
    }

    public InterceptorInvocationContext(InterceptionChain interceptionChain, Constructor<?> constructor, Object[] parameters, Map<String, Object> contextData) {
        this(interceptionChain, null, null, constructor, parameters, null, contextData);
    }

    private InterceptorInvocationContext(InterceptionChain interceptionChain, Object target, Method method, Constructor<?> constructor, Object[] parameters, Object timer) {
        this(interceptionChain, target, method, constructor, parameters, timer, new HashMap<String, Object>());
    }

    private InterceptorInvocationContext(InterceptionChain interceptionChain, Object target, Method method, Constructor<?> constructor, Object[] parameters, Object timer, Map<String, Object> contextData) {
        this.interceptionChain = interceptionChain;
        this.target = target;
        this.method = method;
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

    @Override
    public Object proceed() throws Exception {
        try {
            return interceptionChain.invokeNextInterceptor(this);
        } catch (Exception e) {
            throw e;
        } catch (Throwable t) {
            throw new InterceptorException(t);
        }
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

    private static Class<?> getWrapperClass(Class<?> primitiveClass) {
        if (!WRAPPER_CLASSES.containsKey(primitiveClass)) {
            return primitiveClass;
        } else {
            return WRAPPER_CLASSES.get(primitiveClass);
        }
    }

    private static Class<?> getPrimitiveClass(Class<?> wrapperClass) {
        if (!REVERSE_WRAPPER_CLASSES.containsKey(wrapperClass)) {
            return wrapperClass;
        } else {
            return REVERSE_WRAPPER_CLASSES.get(wrapperClass);
        }
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
                throw new IllegalArgumentException("Wrong number of parameters: method has " + parameterTypes.length
                        + ", attempting to set " + newParametersCount + (params != null ? "" : " (argument was null)"));
            }
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    Class<?> methodParameterClass = parameterTypes[i];
                    if (params[i] != null) {
                        //identity ok
                        Class<? extends Object> newArgumentClass = params[i].getClass();
                        if (newArgumentClass.equals(methodParameterClass)) {
                            break;
                        }
                        if (newArgumentClass.isPrimitive()) {
                            // argument is primitive - never actually a case for interceptors
                            if (methodParameterClass.isPrimitive()) {
                                //widening primitive
                                if (!isWideningPrimitive(newArgumentClass, methodParameterClass)) {
                                    throwIAE(i, methodParameterClass, newArgumentClass);
                                }
                            } else {
                                //boxing+widening reference
                                Class<?> boxedArgumentClass = getWrapperClass(newArgumentClass);
                                if (!methodParameterClass.isAssignableFrom(boxedArgumentClass)) {
                                    throwIAE(i, methodParameterClass, newArgumentClass);
                                }
                            }
                        } else {
                            // argument is non-primitive
                            if (methodParameterClass.isPrimitive()) {
                                // unboxing+widening primitive
                                Class<?> unboxedClass = getPrimitiveClass(newArgumentClass);

                                if (!unboxedClass.equals(methodParameterClass) && !isWideningPrimitive(unboxedClass, methodParameterClass)) {
                                    throwIAE(i, methodParameterClass, newArgumentClass);
                                }
                            } else {
                                //widening reference
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
        throw new IllegalArgumentException("Incompatible parameter type on position: " + i + " :" + newArgumentClass + " (expected type was " + methodParameterClass.getName() + ")");
    }

    @Override
    public Object getTimer() {
        return timer;
    }

    @Override
    public Constructor<?> getConstructor() {
        return constructor;
    }
}
