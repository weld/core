/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import java.util.ArrayDeque;
import java.util.Deque;

import org.jboss.weld.contexts.cache.RequestScopedCache;
import org.jboss.weld.contexts.cache.RequestScopedItem;

/**
 * A stack that is kept in thread-local. Two operations were identified to be expensive in micro benchmarks:
 * <ul>
 * <li>ThreadLocal.set()</li>
 * <li>ThreadLocal.get() if the current value is null (because such get involves setting the initial value)</li>
 * </ul>
 *
 * Therefore this implementation tries to optimize that.
 *
 * First of all we make use of {@link RequestScopedCache} for cleaning up the thread local. If {@link RequestScopedCache} is
 * active
 * we do not remove the stack from thread local immediately when it becomes empty but defer this to the point when
 * {@link RequestScopedCache}
 * is cleaned up.
 *
 * Secondly, we reduce the number of ThreadLocal.get() accesses by returning a {@link ThreadLocalStackReference} which a client
 * uses to pop a value.
 *
 * Lastly, the {@link ThreadLocal} instance is configured to set a new initial value by default. This is safe when
 * {@link RequestScopedCache} is used
 * but may lead to {@link ThreadLocal} leak when it is not. Therefore, special care needs to be take to guarantee that each
 * {@link ThreadLocal#get()}
 * operation has a matching {@link Stack#removeIfEmpty()} call (see {@link ThreadLocalStack#peek()}) as an example.
 *
 */
public class ThreadLocalStack<T> {

    private final ThreadLocal<Stack<T>> threadLocalStack;

    public ThreadLocalStack() {
        this.threadLocalStack = new ThreadLocal<Stack<T>>() {
            @Override
            protected Stack<T> initialValue() {
                return new Stack<T>(this);
            }
        };
    }

    /**
     * Reference to a thread-local stack. Each client that calls {@link ThreadLocalStack#push(Object)} is required
     * to call {@link ThreadLocalStackReference#pop()} to clean up the value (e.g. in a finally block).
     */
    public interface ThreadLocalStackReference<T> {
        T pop();
    }

    private static class Stack<T> implements RequestScopedItem, ThreadLocalStackReference<T> {
        private final Deque<T> elements;
        private final ThreadLocal<Stack<T>> interceptionContexts;
        private boolean removeWhenEmpty;
        private boolean valid;

        private Stack(ThreadLocal<Stack<T>> interceptionContexts) {
            this.interceptionContexts = interceptionContexts;
            this.elements = new ArrayDeque<T>();
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

        private void checkState() {
            if (!valid) {
                throw new IllegalStateException("This ThreadLocalStack is no longer valid.");
            }
        }

        public void push(T item) {
            checkState();
            elements.addFirst(item);
        }

        public T peek() {
            checkState();
            return elements.peekFirst();
        }

        public T pop() {
            checkState();
            T top = elements.removeFirst();
            removeIfEmpty();
            return top;
        }

        private void removeIfEmpty() {
            if (removeWhenEmpty && elements.isEmpty()) {
                interceptionContexts.remove();
                valid = false;
            }
        }

        @Override
        public void invalidate() {
            /*
             * This cached item is being invalidated.
             * It does not necessarily mean that the request is being destroyed - it may just be the case that it is being
             * flushed in the middle
             * of a request (e.g. for AlterableContext.destroy()).
             * Therefore, we cannot remove the stack now but we just set removeWhenEmpty flag and let it remove itself once the
             * stack gets empty.
             */
            removeWhenEmpty = true;
            removeIfEmpty();
        }
    }

    public ThreadLocalStackReference<T> push(T item) {
        Stack<T> stack = threadLocalStack.get();
        stack.push(item);
        return stack;
    }

    public T peek() {
        Stack<T> stack = threadLocalStack.get();
        T top = stack.peek();
        // If RequestScopedCache is not active we should remove immediately in order to prevent a leak
        stack.removeIfEmpty();
        return top;
    }

    /**
     * Convenience method which only pushes something to stack if the condition evaluates to true. If the condition evaluates to
     * true, this method behaves the same as {@link #push(Object)}.
     * Otherwise, a special null {@link ThreadLocalStackReference} object is returned. {@link ThreadLocalStackReference#pop()}
     * may
     * be called on the returned object - it will not have any effect and always return null.
     */
    @SuppressWarnings("unchecked")
    public ThreadLocalStackReference<T> pushConditionally(T item, boolean condition) {
        if (condition) {
            return push(item);
        } else {
            return (ThreadLocalStackReference<T>) NULL_REFERENCE;
        }
    }

    /**
     * Convenience method which also accepts null values. If the given parameter is non-null, this method behaves the same as
     * {@link #push(Object)}.
     * Otherwise, a special null {@link ThreadLocalStackReference} object is returned. {@link ThreadLocalStackReference#pop()}
     * may
     * be called on the returned object - it will not have any effect and always return null.
     */
    public ThreadLocalStackReference<T> pushIfNotNull(T item) {
        return pushConditionally(item, item != null);
    }

    private static final ThreadLocalStackReference<Object> NULL_REFERENCE = new ThreadLocalStackReference<Object>() {
        @Override
        public Object pop() {
            return null;
        }
    };
}
