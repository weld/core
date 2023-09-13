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

import org.jboss.weld.contexts.cache.RequestScopedCache;
import org.jboss.weld.contexts.cache.RequestScopedItem;

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
 *
 * @author Marius Bogoevici
 */
public class InterceptionDecorationContext {
    private static ThreadLocal<Stack> interceptionContexts = new ThreadLocal<Stack>();

    public static class Stack implements RequestScopedItem {
        private boolean removeWhenEmpty;
        private final Deque<CombinedInterceptorAndDecoratorStackMethodHandler> elements;
        private final ThreadLocal<Stack> interceptionContexts;
        private boolean valid;

        private Stack(ThreadLocal<Stack> interceptionContexts) {
            this.interceptionContexts = interceptionContexts;
            this.elements = new ArrayDeque<CombinedInterceptorAndDecoratorStackMethodHandler>();
            /*
             * Setting / removing of a thread-local is much more expensive compared to get. Therefore,
             * if RequestScopedCache is active we register the thread-local for removal at the end of the
             * request. This yields possitive results only if the number of intercepted invocations is large.
             * If it is not, the performance characteristics are similar to explicitly removing the thread-local
             * once the stack gets empty.
             */
            this.removeWhenEmpty = !RequestScopedCache.addItemIfActive(this);
            this.valid = true;
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
            checkState();
            if (elements.isEmpty() || peek() != context) {
                push(context);
                return true;
            }
            return false;
        }

        public void end() {
            pop();
        }

        private void push(CombinedInterceptorAndDecoratorStackMethodHandler item) {
            checkState();
            elements.addFirst(item);
        }

        public CombinedInterceptorAndDecoratorStackMethodHandler peek() {
            checkState();
            return elements.peekFirst();
        }

        private CombinedInterceptorAndDecoratorStackMethodHandler pop() {
            checkState();
            CombinedInterceptorAndDecoratorStackMethodHandler top = elements.removeFirst();
            removeIfEmpty();
            return top;
        }

        private void checkState() {
            if (!valid) {
                throw new IllegalStateException("This InterceptionDecorationContext is no longer valid.");
            }
        }

        @Override
        public void invalidate() {
            /*
             * This cached item is being invalidated.
             * It does not necessarily mean that the request is being destroyed - it may just be the case that it is being
             * flushed in the middle
             * of a request (e.g. for AlterableContext.destroy()).
             * Therefore, we cannot remove IDC now but we just set removeWhenEmpty flag and let it remove itself once the stack
             * gets empty.
             */
            removeWhenEmpty = true;
            removeIfEmpty();
        }

        private void removeIfEmpty() {
            if (removeWhenEmpty && elements.isEmpty()) {
                interceptionContexts.remove();
                valid = false;
            }
        }

        public int size() {
            return elements.size();
        }

        @Override
        public String toString() {
            return "Stack [valid=" + valid + ", cached=" + !removeWhenEmpty + ", elements=" + elements + "]";
        }

    }

    private InterceptionDecorationContext() {
    }

    /**
     * Peeks the current top of the stack.
     *
     * @return the current top of the stack
     * @throws EmptyStackException
     */
    public static CombinedInterceptorAndDecoratorStackMethodHandler peek() {
        return peek(interceptionContexts.get());
    }

    /**
     * Peeks the current top of the stack or returns null if the stack is empty
     *
     * @return the current top of the stack or returns null if the stack is empty
     */
    public static CombinedInterceptorAndDecoratorStackMethodHandler peekIfNotEmpty() {
        Stack stack = interceptionContexts.get();
        if (stack == null) {
            return null;
        }
        return stack.peek();
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
        Stack stack = getStack();
        if (!stack.elements.isEmpty()) {
            stack.push(CombinedInterceptorAndDecoratorStackMethodHandler.NULL_INSTANCE);
            return stack;
        } else {
            // if RequestScopedCache is not active, remove now to prevent ThreadLocal leak
            stack.removeIfEmpty();
            return null;
        }
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
     * Gets the current Stack. If the stack is not set, a new empty instance is created and set.
     *
     * @return
     */
    public static Stack getStack() {
        Stack stack = interceptionContexts.get();
        if (stack == null) {
            stack = new Stack(interceptionContexts);
            interceptionContexts.set(stack);
        }
        return stack;
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

    private static boolean empty(Stack stack) {
        if (stack == null) {
            return true;
        } else {
            return stack.elements.isEmpty();
        }
    }
}
