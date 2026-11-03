/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026, Red Hat, Inc., and individual contributors
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

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jboss.weld.util.Primitives;

/**
 * Invokes an instance method with exactly the semantics of {@link Method#invoke(Object, Object...)}, but through a
 * {@link MethodHandle} whenever the invocation cannot fail before reaching the method.
 * <p>
 * The handle is only used if the receiver is an instance of the declaring class and every argument is either an instance
 * of the corresponding reference parameter type (or {@code null}) or an instance of the exact wrapper type of the
 * corresponding primitive parameter type. In this case the conversions applied by the handle cannot fail and any
 * {@link Throwable} thrown by the invocation was thrown by the method itself; it is wrapped in an
 * {@link InvocationTargetException}, as {@link Method#invoke(Object, Object...)} does. In all other cases (e.g. widening
 * primitive conversions, wrong arguments, {@code null} receiver, static methods, a method handle that cannot be created)
 * the given {@link Method} is invoked reflectively, so that the exceptions thrown remain exactly the same.
 * <p>
 * Instances are immutable and shared; they are cached per method (see {@link #of(Method)}) and created lazily, upon the
 * first invocation of the method, so that the bootstrap is not affected.
 *
 * @author Weld contributors
 */
public final class MethodInvoker {

    private static final Object[] NO_ARGUMENTS = new Object[0];
    private static final MethodType SPREAD_TYPE = MethodType.methodType(Object.class, Object.class, Object[].class);
    private static final MethodType SINGLE_ARGUMENT_TYPE = MethodType.methodType(Object.class, Object.class, Object.class);

    /*
     * Invokers are cached per declaring class. The cache is attached to the declaring class itself so that it does not
     * prevent the class (loader) from being garbage collected.
     */
    private static final ClassValue<ConcurrentMap<Method, MethodInvoker>> INVOKERS = new ClassValue<ConcurrentMap<Method, MethodInvoker>>() {
        @Override
        protected ConcurrentMap<Method, MethodInvoker> computeValue(Class<?> type) {
            return new ConcurrentHashMap<>();
        }
    };

    /**
     * Returns the (cached) invoker for the given method. The method has to be accessible to Weld already (i.e. it can be
     * invoked reflectively); this method never changes its accessibility.
     *
     * @param method the method, may be null
     * @return the invoker of the given method, null if the given method is null
     */
    public static MethodInvoker of(Method method) {
        if (method == null) {
            return null;
        }
        ConcurrentMap<Method, MethodInvoker> invokers = INVOKERS.get(method.getDeclaringClass());
        MethodInvoker invoker = invokers.get(method);
        if (invoker == null) {
            invoker = new MethodInvoker(method);
            MethodInvoker previous = invokers.putIfAbsent(method, invoker);
            if (previous != null) {
                invoker = previous;
            }
        }
        return invoker;
    }

    private final Class<?> receiverType;
    private final boolean accessChecksSuppressed;
    // the expected classes of the arguments; the wrapper class for a primitive parameter
    private final Class<?>[] argumentTypes;
    private final boolean[] primitive;
    // (Object, Object[])Object, null if reflection has to be used
    private final MethodHandle spreadHandle;
    // (Object, Object)Object, null if the method does not declare exactly one parameter or reflection has to be used
    private final MethodHandle singleArgumentHandle;
    /*
     * Lambdas (LambdaMetafactory) invoking the method directly, for methods with a non-void return type and no parameter or
     * a single reference type parameter (e.g. @AroundInvoke methods and getters), null if not applicable or not possible.
     * Unlike a method handle stored in a field, a call through them can be inlined by the JIT.
     */
    private final Function<Object, Object> noArgumentFunction;
    private final BiFunction<Object, Object, Object> singleArgumentFunction;

    @SuppressWarnings("deprecation")
    private MethodInvoker(Method method) {
        this.accessChecksSuppressed = method.isAccessible();
        this.receiverType = method.getDeclaringClass();
        Class<?>[] parameterTypes = method.getParameterTypes();
        this.argumentTypes = new Class<?>[parameterTypes.length];
        this.primitive = new boolean[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            primitive[i] = parameterTypes[i].isPrimitive();
            argumentTypes[i] = primitive[i] ? Primitives.wrap(parameterTypes[i]) : parameterTypes[i];
        }
        MethodHandle handle = null;
        if (!Modifier.isStatic(method.getModifiers())) {
            try {
                // no access check if the method is accessible; otherwise the same check as reflection, performed by Weld
                handle = MethodHandles.lookup().unreflect(method);
            } catch (IllegalAccessException | RuntimeException e) {
                // e.g. a module not readable by Weld - use reflection
                handle = null;
            }
        }
        if (handle != null) {
            this.spreadHandle = handle.asSpreader(Object[].class, parameterTypes.length).asType(SPREAD_TYPE);
            this.singleArgumentHandle = parameterTypes.length == 1 ? handle.asType(SINGLE_ARGUMENT_TYPE) : null;
        } else {
            this.spreadHandle = null;
            this.singleArgumentHandle = null;
        }
        Object function = handle != null ? createFunction(method, parameterTypes) : null;
        this.noArgumentFunction = parameterTypes.length == 0 ? asFunction(function) : null;
        this.singleArgumentFunction = parameterTypes.length == 1 ? asBiFunction(function) : null;
    }

    @SuppressWarnings("unchecked")
    private static Function<Object, Object> asFunction(Object function) {
        return (Function<Object, Object>) function;
    }

    @SuppressWarnings("unchecked")
    private static BiFunction<Object, Object, Object> asBiFunction(Object function) {
        return (BiFunction<Object, Object, Object>) function;
    }

    private static Object createFunction(Method method, Class<?>[] parameterTypes) {
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class || parameterTypes.length > 1
                || (parameterTypes.length == 1 && parameterTypes[0].isPrimitive())) {
            return null;
        }
        try {
            // requires the package of the declaring class to be open to Weld; the lambda class is defined in that package
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
            MethodHandle implementation = lookup.unreflect(method);
            Class<?> boxedReturnType = Primitives.wrap(returnType);
            CallSite callSite;
            if (parameterTypes.length == 0) {
                callSite = LambdaMetafactory.metafactory(lookup, "apply", MethodType.methodType(Function.class),
                        MethodType.methodType(Object.class, Object.class), implementation,
                        MethodType.methodType(boxedReturnType, method.getDeclaringClass()));
            } else {
                callSite = LambdaMetafactory.metafactory(lookup, "apply", MethodType.methodType(BiFunction.class),
                        MethodType.methodType(Object.class, Object.class, Object.class), implementation,
                        MethodType.methodType(boxedReturnType, method.getDeclaringClass(), parameterTypes[0]));
            }
            return callSite.getTarget().invoke();
        } catch (Throwable e) {
            // e.g. a package not open to Weld, a security manager - use the method handle
            return null;
        }
    }

    /**
     * Equivalent to {@code method.invoke(target, args)}.
     *
     * @param method the method this invoker was created for (or an equal one), invoked if the method handle cannot be used
     */
    public Object invoke(Method method, Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
        Function<Object, Object> function = noArgumentFunction;
        if (function != null && permitsCachedAccess(method) && args != null && args.length == 0
                && receiverType.isInstance(target)) {
            try {
                return function.apply(target);
            } catch (Throwable e) {
                throw new InvocationTargetException(e);
            }
        }
        MethodHandle handle = spreadHandle;
        if (handle != null && permitsCachedAccess(method) && receiverType.isInstance(target) && accepts(args)) {
            try {
                return (Object) handle.invokeExact(target, args);
            } catch (Throwable e) {
                throw new InvocationTargetException(e);
            }
        }
        return method.invoke(target, args);
    }

    /**
     * Equivalent to {@code method.invoke(target, arg)}.
     *
     * @param method the method this invoker was created for (or an equal one), invoked if the method handle cannot be used
     */
    public Object invoke(Method method, Object target, Object arg) throws IllegalAccessException, InvocationTargetException {
        BiFunction<Object, Object, Object> function = singleArgumentFunction;
        if (function != null && permitsCachedAccess(method) && receiverType.isInstance(target) && accepts(0, arg)) {
            try {
                return function.apply(target, arg);
            } catch (Throwable e) {
                throw new InvocationTargetException(e);
            }
        }
        MethodHandle handle = singleArgumentHandle;
        if (handle != null && permitsCachedAccess(method) && receiverType.isInstance(target) && accepts(0, arg)) {
            try {
                return (Object) handle.invokeExact(target, arg);
            } catch (Throwable e) {
                throw new InvocationTargetException(e);
            }
        }
        return method.invoke(target, arg);
    }

    /**
     * Equivalent to {@code method.invoke(target)}.
     *
     * @param method the method this invoker was created for (or an equal one), invoked if the method handle cannot be used
     */
    public Object invoke(Method method, Object target) throws IllegalAccessException, InvocationTargetException {
        MethodHandle handle = spreadHandle;
        if (handle != null && permitsCachedAccess(method) && argumentTypes.length == 0 && receiverType.isInstance(target)) {
            try {
                return (Object) handle.invokeExact(target, NO_ARGUMENTS);
            } catch (Throwable e) {
                throw new InvocationTargetException(e);
            }
        }
        return method.invoke(target);
    }

    @SuppressWarnings("deprecation")
    private boolean permitsCachedAccess(Method method) {
        // Method.equals() ignores the access override. A handle created with suppressed access checks must not
        // authorize an equal Method without that override, or survive setAccessible(false) on the original Method.
        return !accessChecksSuppressed || method.isAccessible();
    }

    private boolean accepts(Object[] args) {
        if (args == null || args.length != argumentTypes.length) {
            return false;
        }
        for (int i = 0; i < args.length; i++) {
            if (!accepts(i, args[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean accepts(int position, Object arg) {
        if (arg == null) {
            return !primitive[position];
        }
        // no widening primitive conversions
        return primitive[position] ? arg.getClass() == argumentTypes[position] : argumentTypes[position].isInstance(arg);
    }
}
