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
package org.jboss.weld.bootstrap;

import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.executor.DeamonThreadFactory;
import org.jboss.weld.logging.messages.BootstrapMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;
import org.slf4j.cal10n.LocLogger;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Allows observer methods for container lifecycle events to be resolved upfront while the deployment is waiting for classloader
 * or reflection API.
 *
 * @author Jozef Hartinger
 *
 */
public class ContainerLifecycleEventPreloader implements Service {

    // This is an optional services thus we do not need tasks to finish in order to get a valid deployment
    private static final long SHUTDOWN_TIMEOUT = 1L;
    private static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);

    private static class PreloadingTask implements Callable<Void> {

        private final Type type;
        private final BeanManagerImpl manager;

        public PreloadingTask(Type type, BeanManagerImpl manager) {
            this.type = type;
            this.manager = manager;
        }

        @Override
        public Void call() throws Exception {
            manager.resolveObserverMethods(type);
            return null;
        }
    }

    private final ExecutorService executor;

    public ContainerLifecycleEventPreloader(BootstrapConfiguration configuration) {
        this.executor = Executors.newFixedThreadPool(configuration.getPreloaderThreads(), new DeamonThreadFactory(new ThreadGroup("weld-preloaders"), "weld-preloader-"));
    }

    @SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "We never need to synchronize with the preloader.")
    public void preloadContainerLifecycleEvent(BeanManagerImpl manager, Class<?> eventRawType, Type... typeParameters) {
        executor.submit(new PreloadingTask(new ParameterizedTypeImpl(eventRawType, typeParameters, null), manager));
    }

    @Override
    public void cleanup() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                    log.warn(BootstrapMessage.TIMEOUT_SHUTTING_DOWN_THREAD_POOL, executor, this);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void shutdown() {
        ContainerLifecycleEventPreloader instance = Container.instance().services().get(ContainerLifecycleEventPreloader.class);
        if (instance != null) {
            instance.cleanup();
        }
    }
}
