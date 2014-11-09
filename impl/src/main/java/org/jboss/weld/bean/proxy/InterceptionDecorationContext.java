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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EmptyStackException;

import org.jboss.weld.context.cache.RequestScopedCache;

/**
 * A class that holds the interception (and decoration) contexts which are currently in progress.
 * <p/>
 * An interception context is a set of {@link CombinedInterceptorAndDecoratorStackMethodHandler} references for which interception is currently
 * suppressed (so that self-invocation is not possible).
 * Such references are added as soon as a CombinedMethodHandler is executed in an interception context that
 * does not hold it.
 * <p/>
 * Classes may create new interception contexts as necessary (e.g. allowing client proxies to create new interception
 * contexts in order to make circular references interceptable multiple times).
 *
 * @author Marius Bogoevici
 */
public class InterceptionDecorationContext {
    private static ThreadLocal<Stack> interceptionContexts = new ThreadLocal<Stack>();

    private static class Stack {
        private final boolean removeWhenEmpty;
        private final Deque<CombinedInterceptorAndDecoratorStackMethodHandler> elements;

        private Stack() {
            this.elements = new ArrayDeque<CombinedInterceptorAndDecoratorStackMethodHandler>();
            /*
             * Setting / removing of a thread-local is much more expensive compared to get. Therefore,
             * if RequestScopedCache is active we register the thread-local for removal at the end of the
             * request. This yields possitive results only if the number of intercepted invocations is large.
             * If it is not, the performance characteristics are similar to explicitly removing the thread-local
             * once the stack gets empty.
             */
            this.removeWhenEmpty = !RequestScopedCache.addItemIfActive(interceptionContexts);
        }

        private boolean shouldRemove() {
            return removeWhenEmpty && elements.isEmpty();
        }
    }

    private InterceptionDecorationContext() {
    }

    /**
     * Peeks the current top of the stack.
     * @return the current top of the stack
     * @throws EmptyStackException
     */
    public static CombinedInterceptorAndDecoratorStackMethodHandler peek() {
        return peek(interceptionContexts.get());
    }

    /**
     * Peeks the current top of the stack or returns null if the stack is empty
     * @return the current top of the stack or returns null if the stack is empty
     */
    public static CombinedInterceptorAndDecoratorStackMethodHandler peekIfNotEmpty() {
        Stack stack = interceptionContexts.get();
        if (empty(stack)) {
            return null;
        }
        return peek(stack);
    }

    /**
     * Indicates whether the stack is empty.
     */
    public static boolean empty() {
        return empty(interceptionContexts.get());
    }


    public static void endInterceptorContext() {
        pop(interceptionContexts.get());
    }

    /**
     * This is called by client proxies. Calling a method on a client proxy means that we left the interception context of the calling bean. Therefore,
     * client proxies call this method to start a new interception context of the called (possibly intercepted) bean. If however there is not interception context
     * at the time the proxy is called (meaning the caller is not intercepted), there is no need to create new interception context. This is an optimization as the
     * first startInterceptorContext call is expensive.
     *
     * The caller of this method is required to call {@link #endInterceptorContext()} if and only if this method returns true.
     */
    public static boolean startIfNotEmpty() {
        Stack stack = interceptionContexts.get();
        if (empty(stack)) {
            return false;
        }
        push(interceptionContexts.get(), CombinedInterceptorAndDecoratorStackMethodHandler.NULL_INSTANCE);
        return true;
    }

    /**
     * Pushes the given context to the stack if the given context is not on top of the stack already.
     * If push happens, the caller is responsible for calling {@link #endInterceptorContext()} after the invocation finishes.
     * @param context the given context
     * @return true if the given context was pushed to the top of the stack, false if the given context was on top already
     */
    public static boolean startIfNotOnTop(CombinedInterceptorAndDecoratorStackMethodHandler context) {
        Stack stack = interceptionContexts.get();
        if (empty(stack) || peek(stack) != context) { // == used intentionally instead of equals
            push(stack, context);
            return true;
        }
        return false;
    }

    private static CombinedInterceptorAndDecoratorStackMethodHandler pop(Stack stack) {
        if (stack == null) {
            throw new EmptyStackException();
        } else {
            try {
                return stack.elements.removeFirst();
            } finally {
                if (stack.shouldRemove()) {
                    interceptionContexts.remove();
                }
            }
        }
    }

    private static void push(Stack stack, CombinedInterceptorAndDecoratorStackMethodHandler item) {
        if (stack == null) {
            stack = new Stack();
            interceptionContexts.set(stack);
        }
        stack.elements.addFirst(item);
    }

    private static CombinedInterceptorAndDecoratorStackMethodHandler peek(Stack stack) {
        if (stack == null) {
            throw new EmptyStackException();
        } else {
            return stack.elements.peekFirst();
        }
    }

    private static boolean empty(Stack stack) {
        if (stack == null) {
            return true;
        } else {
            return stack.elements.isEmpty();
        }
    }
}
