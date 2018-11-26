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
package org.jboss.weld.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.Environments;

/**
 * Wrapper for {@link ForkJoinPool#commonPool()}. This {@link ExecutorService} implementation ignores threadPoolSize and
 * threadPoolKeepAliveTime configuration options.
 *
 * @author Jozef Hartinger
 *
 */
public class CommonForkJoinPoolExecutorServices extends AbstractExecutorServices {

    @Override
    public ExecutorService getTaskExecutor() {
        return ForkJoinPool.commonPool();
    }

    @Override
    public void cleanup() {
        // noop
    }

    @Override
    protected int getThreadPoolSize() {
        return ForkJoinPool.getCommonPoolParallelism();
    }

    /**
     * If in SE environment, this method wraps the collection of tasks in such a way that it sets the TCCL to null before
     * executing them and re-sets it to previous value once done. This affect the CL Weld will pick up when invoking
     * {@link org.jboss.weld.environment.deployment.WeldResourceLoader#getClassLoader()}.
     *
     * @see WELD-2494
     */
    @Override
    public <T> Collection<? extends Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
        // try to detect environment, if it's SE, we want to null TCCL, otherwise leave it as it is
        if (Container.getEnvironment().equals(Environments.SE)) {
            List<Callable<T>> wrapped = new ArrayList<>(tasks.size());
            for (Callable<T> task : tasks) {
                wrapped.add(() -> {
                    ClassLoader oldTccl = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(null);
                    try {
                        return task.call();
                    } finally {
                        Thread.currentThread().setContextClassLoader(oldTccl);
                    }
                });
            }
            return wrapped;
        } else {
            return tasks;
        }
    }
}
