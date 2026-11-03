/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.weld.bean.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.NoSuchElementException;

/**
 * A class that holds the interception (and decoration) contexts which are currently in progress.
 * <p/>
 * An interception context is a set of {@link CombinedInterceptorAndDecoratorStackMethodHandler} references for which
 * interception is currently
 * suppressed (so that self-invocation is not possible).
 * Such references are added as soon as a CombinedMethodHandler is executed in an interception context that
 * does not hold it.
 * <p/>
 * Classes may create new interception contexts as necessary (e.g. allowing client proxies to create new interception
 * contexts in order to make circular references interceptable multiple times).
 * <p/>
 * Each thread uses (at most) one {@link Stack} which is reused across invocations. The thread-local only ever holds a
 * small {@code Object[]} holder (a JDK class): the stack is referenced strongly only while it is not empty and weakly
 * otherwise. An idle thread therefore never retains a Weld class (no class loader leak) and an intercepted invocation
 * does not need to allocate a new stack nor to set / clear the thread-local on every call.
 *
 * @author Marius Bogoevici
 */
public class InterceptionDecorationContext {

    // holder[ACTIVE] - the stack of this thread while it is not empty, null otherwise
    private static final int ACTIVE = 0;
    // holder[REUSABLE] - a WeakReference to the (possibly empty) stack of this thread
    private static final int REUSABLE = 1;

    private static final ThreadLocal<Object[]> interceptionContexts = new ThreadLocal<Object[]>();

    /*
     * Valid until any thread creates its stack holder for the first time (it is never re-validated). While it is valid,
     * no thread can have an interception context, so client proxies do not need to look up the thread-local at all.
     * This is the case whenever no intercepted or decorated method has been invoked yet, e.g. in deployments without
     * interceptors and decorators.
     *
     * A SwitchPoint is used instead of a plain static flag because the JIT compiles the guard to nothing: neither
     * deployments without interception nor deployments using it pay for the check. The invalidation (a
     * deoptimization of the dependent code) happens once, in getStack(), before the holder is published to the
     * current thread; a thread can only have an active stack if it has created its holder itself.
     */
    private static final SwitchPoint NO_HOLDER_CREATED = new SwitchPoint();
    private static volatile boolean holderCreated;
    private static final MethodHandle START_IF_NOT_EMPTY;

    static {
        try {
            MethodHandle lookup = MethodHandles.lookup().findStatic(InterceptionDecorationContext.class,
                    "startIfNotEmptyLookup", MethodType.methodType(Stack.class));
            START_IF_NOT_EMPTY = NO_HOLDER_CREATED.guardWithTest(MethodHandles.constant(Stack.class, null), lookup);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Stack {
        private static final int INITIAL_CAPACITY = 4;
        private final Object[] holder;
        // the thread this stack belongs to
        private final Thread owner;
        // elements[0] is the bottom of the stack, elements[size - 1] the top; unused slots are always null
        private CombinedInterceptorAndDecoratorStackMethodHandler[] elements;
        private int size;

        private Stack(Object[] holder) {
            this.holder = holder;
            this.owner = Thread.currentThread();
            // the stack is usually very shallow
            this.elements = new CombinedInterceptorAndDecoratorStackMethodHandler[INITIAL_CAPACITY];
        }

        /**
         * Pushes the given context to the stack if the given context is not on top of the stack already.
         * If push happens, the caller is responsible for calling {@link #endInterceptorContext()} after the invocation
         * finishes.
         *
         * @param context the given context
         * @return true if the given context was pushed to the top of the stack, false if the given context was on top already
         */
        public boolean startIfNotOnTop(CombinedInterceptorAndDecoratorStackMethodHandler context) {
            int s = size;
            if (s == 0) {
                push(context, 0);
                return true;
            }
            if (elements[s - 1] != context) {
                push(context, s);
                return true;
            }
            return false;
        }

        public void end() {
            pop();
        }

        private void push(CombinedInterceptorAndDecoratorStackMethodHandler item, int s) {
            if (item == null) {
                // null elements are not supported (consistent with the previous ArrayDeque based implementation)
                throw new NullPointerException();
            }
            CombinedInterceptorAndDecoratorStackMethodHandler[] es = elements;
            if (s == es.length) {
                es = grow();
            }
            es[s] = item;
            size = s + 1;
            if (s == 0) {
                // the stack becomes active - reference it strongly so that it cannot be garbage collected
                holder[ACTIVE] = this;
            }
        }

        private CombinedInterceptorAndDecoratorStackMethodHandler[] grow() {
            CombinedInterceptorAndDecoratorStackMethodHandler[] es = Arrays.copyOf(elements, elements.length << 1);
            elements = es;
            return es;
        }

        public CombinedInterceptorAndDecoratorStackMethodHandler peek() {
            int s = size;
            return s == 0 ? null : elements[s - 1];
        }

        private CombinedInterceptorAndDecoratorStackMethodHandler pop() {
            int s = size - 1;
            if (s < 0) {
                throw new NoSuchElementException();
            }
            CombinedInterceptorAndDecoratorStackMethodHandler[] es = elements;
            CombinedInterceptorAndDecoratorStackMethodHandler top = es[s];
            // do not retain the handler (and its bean instance) once it is not on the stack anymore
            es[s] = null;
            size = s;
            if (s == 0) {
                // only weakly referenced from now on, the thread-local does not retain any Weld class
                holder[ACTIVE] = null;
            }
            return top;
        }

        public int size() {
            return size;
        }

        /**
         * @return true if this is the stack of the current thread (the stack a {@link #getStack()} call on the current thread
         *         returns as long as the caller holds a reference to this stack)
         */
        public boolean isOwnedByCurrentThread() {
            return owner == Thread.currentThread();
        }

        @Override
        public String toString() {
            // top of the stack first
            StringBuilder builder = new StringBuilder("Stack [elements=[");
            for (int i = size - 1; i >= 0; i--) {
                builder.append(elements[i]);
                if (i > 0) {
                    builder.append(", ");
                }
            }
            return builder.append("]]").toString();
        }

    }

    private InterceptionDecorationContext() {
    }

    /**
     * @return the stack of the current thread if it is not empty, null otherwise
     */
    private static Stack activeStack() {
        Object[] holder = interceptionContexts.get();
        return holder == null ? null : (Stack) holder[ACTIVE];
    }

    /**
     * Peeks the current top of the stack.
     *
     * @return the current top of the stack
     * @throws EmptyStackException
     */
    public static CombinedInterceptorAndDecoratorStackMethodHandler peek() {
        return peek(activeStack());
    }

    /**
     * Peeks the current top of the stack or returns null if the stack is empty
     *
     * @return the current top of the stack or returns null if the stack is empty
     */
    public static CombinedInterceptorAndDecoratorStackMethodHandler peekIfNotEmpty() {
        Stack stack = activeStack();
        if (stack == null) {
            return null;
        }
        return stack.peek();
    }

    /**
     * Indicates whether the stack is empty.
     */
    public static boolean empty() {
        return activeStack() == null;
    }

    public static void endInterceptorContext() {
        pop(activeStack());
    }

    /**
     * This is called by client proxies. Calling a method on a client proxy means that we left the interception context of the
     * calling bean. Therefore,
     * client proxies call this method to start a new interception context of the called (possibly intercepted) bean. If however
     * there is not interception context
     * at the time the proxy is called (meaning the caller is not intercepted), there is no need to create new interception
     * context. This is an optimization as the
     * first startInterceptorContext call is expensive.
     *
     * If this method returns a non-null value, the caller of this method is required to call {@link Stack#end()} on the
     * returned value.
     */
    public static Stack startIfNotEmpty() {
        try {
            // constant null until the first stack holder is created, startIfNotEmptyLookup() afterwards
            return (Stack) START_IF_NOT_EMPTY.invokeExact();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private static Stack startIfNotEmptyLookup() {
        Stack stack = activeStack();
        if (stack == null) {
            // there is no interception context on this thread (the caller is not intercepted)
            return null;
        }
        stack.push(CombinedInterceptorAndDecoratorStackMethodHandler.NULL_INSTANCE, stack.size);
        return stack;
    }

    /**
     * Pushes the given context to the stack if the given context is not on top of the stack already.
     * If this method return a non-null value, the caller is responsible for calling {@link #endInterceptorContext()}
     * after the invocation finishes.
     *
     * @param context the given context
     * @return true if the given context was pushed to the top of the stack, false if the given context was on top already
     */
    public static Stack startIfNotOnTop(CombinedInterceptorAndDecoratorStackMethodHandler context) {
        Stack stack = getStack();
        if (stack.startIfNotOnTop(context)) {
            return stack;
        }
        return null;
    }

    /**
     * Same as {@link #startIfNotOnTop(CombinedInterceptorAndDecoratorStackMethodHandler)} but avoids the thread-local lookup
     * if the given stack (obtained earlier and still referenced by the caller) is the stack of the current thread.
     *
     * @param stack a stack obtained by {@link #getStack()}, possibly on another thread, may be null
     * @param context the given context
     * @return the stack if the given context was pushed to it, null if the given context was on top already
     */
    public static Stack startIfNotOnTop(Stack stack, CombinedInterceptorAndDecoratorStackMethodHandler context) {
        if (stack == null || !stack.isOwnedByCurrentThread()) {
            // e.g. InvocationContext.proceed() called on another thread
            return startIfNotOnTop(context);
        }
        if (stack.startIfNotOnTop(context)) {
            return stack;
        }
        return null;
    }

    /**
     * Gets the current Stack. If there is no stack for the current thread, a new empty instance is created.
     * The returned stack may be empty; it is only guaranteed to stay the stack of the current thread while the caller
     * holds a reference to it.
     *
     * @return the stack of the current thread
     */
    @SuppressWarnings("unchecked")
    public static Stack getStack() {
        Object[] holder = interceptionContexts.get();
        if (holder == null) {
            if (!holderCreated) {
                noHolderCreatedAnymore();
            }
            holder = new Object[2];
            interceptionContexts.set(holder);
        }
        Stack stack = (Stack) holder[ACTIVE];
        if (stack == null) {
            WeakReference<Stack> ref = (WeakReference<Stack>) holder[REUSABLE];
            stack = ref == null ? null : ref.get();
            if (stack == null) {
                stack = new Stack(holder);
                holder[REUSABLE] = new WeakReference<Stack>(stack);
            }
        }
        return stack;
    }

    private static synchronized void noHolderCreatedAnymore() {
        if (!holderCreated) {
            // switch all client proxies to the thread-local lookup before this thread can start an interception context
            SwitchPoint.invalidateAll(new SwitchPoint[] { NO_HOLDER_CREATED });
            holderCreated = true;
        }
    }

    private static CombinedInterceptorAndDecoratorStackMethodHandler pop(Stack stack) {
        if (stack == null) {
            throw new EmptyStackException();
        } else {
            return stack.pop();
        }
    }

    private static CombinedInterceptorAndDecoratorStackMethodHandler peek(Stack stack) {
        if (stack == null) {
            throw new EmptyStackException();
        } else {
            return stack.peek();
        }
    }
}
