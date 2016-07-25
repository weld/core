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
package org.jboss.weld.executor;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.util.collections.Iterables;

/**
 * Used for decomposition of loops in which independent tasks are processed sequentially.
 *
 * Based on the size of a thread pool, the factory creates an equal number of workers. Each worker iterates on a shared
 * concurrent queue. The queue is created from the source items (iterable).
 *
 * @author Jozef Hartinger
 *
 * @param T the type of processed items
 */
public abstract class IterativeWorkerTaskFactory<T> implements ExecutorServices.TaskFactory<Void> {

    private final Queue<T> queue;

    public IterativeWorkerTaskFactory(Iterable<? extends T> iterable) {
        this.queue = new ConcurrentLinkedQueue<T>();
        Iterables.addAll(queue, iterable);
    }

    @Override
    public List<Callable<Void>> createTasks(int threadPoolSize) {
        int taskCount = Runtime.getRuntime().availableProcessors();
        if (threadPoolSize > 0) {
            taskCount = Math.min(Runtime.getRuntime().availableProcessors(), threadPoolSize);
        }
        List<Callable<Void>> tasks = new LinkedList<Callable<Void>>();
        for (int i = 0; i < taskCount; i++) {
            tasks.add(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    init();
                    Thread thread = Thread.currentThread();
                    for (T i = queue.poll(); i != null && !thread.isInterrupted(); i = queue.poll()) {
                        doWork(i);
                    }
                    cleanup();
                    return null;
                }
            });
        }
        return tasks;
    }

    /**
     * Called before the compilation begins.
     */
    protected void init() {
    }

    /**
     * Called after the computation finishes.
     */
    protected void cleanup() {
    }

    protected abstract void doWork(T item);

    public Queue<T> getQueue() {
        return queue;
    }
}
