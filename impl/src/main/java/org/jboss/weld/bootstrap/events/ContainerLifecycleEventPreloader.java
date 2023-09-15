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
package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.weld.event.ObserverNotifier;
import org.jboss.weld.executor.DaemonThreadFactory;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Allows observer methods for container lifecycle events to be resolved upfront while the deployment is waiting for classloader
 * or reflection API.
 *
 * @author Jozef Hartinger
 *
 */
public class ContainerLifecycleEventPreloader {

    private class PreloadingTask implements Callable<Void> {

        private final Type type;

        public PreloadingTask(Type type) {
            this.type = type;
        }

        @Override
        public Void call() throws Exception {
            notifier.resolveObserverMethods(type);
            return null;
        }
    }

    // Keep static to only initialize the group once.
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("weld-preloaders");

    private final ExecutorService executor;
    private final ObserverNotifier notifier;

    public ContainerLifecycleEventPreloader(int threadPoolSize, ObserverNotifier notifier) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize,
                new DaemonThreadFactory(THREAD_GROUP, "weld-preloader-"));
        this.notifier = notifier;
    }

    /**
     * In multi-threaded environment we often cannot leverage multiple core fully in bootstrap because the deployer
     * threads are often blocked by the reflection API or waiting to get a classloader lock. While waiting for classes to be
     * loaded or
     * reflection metadata to be obtained, we can make use of the idle CPU cores and start resolving container lifecycle event
     * observers
     * (extensions) upfront for those types of events we know we will be firing. Since these resolutions are cached, firing of
     * the
     * lifecycle events will then be very fast.
     *
     */
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "We never need to synchronize with the preloader.")
    void preloadContainerLifecycleEvent(Class<?> eventRawType, Type... typeParameters) {
        executor.submit(new PreloadingTask(new ParameterizedTypeImpl(eventRawType, typeParameters, null)));
    }

    void shutdown() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
