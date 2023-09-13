/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.service.executorService;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.executor.ExecutorServicesFactory;
import org.jboss.weld.manager.api.ExecutorServices;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that TCCL is set to null when running in SE with enabled discovery (in which case we use ForkJoinPool and hence
 * CommonForkJoinPoolExecutorServices)
 *
 * @see WELD-2494
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class ContextClassLoaderNullifiedTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder().add(ShrinkWrap.create(BeanArchive.class)
                .addClasses(ContextClassLoaderNullifiedTest.class, DummyBean.class))
                .build();
    }

    @Test
    public void testTcclNullifiedInSeEnvironment() {
        Weld weld = new Weld()
                // in case default changes, make sure this tests uses ForkJoinPool
                .addProperty(ConfigurationKey.EXECUTOR_THREAD_POOL_TYPE.toString(),
                        ExecutorServicesFactory.ThreadPoolType.COMMON.toString());
        try (WeldContainer container = weld.initialize()) {
            AtomicBoolean isTCCLNull = new AtomicBoolean(false);

            // retrieve service and use it to invoke Callable
            ServiceRegistry registry = BeanManagerProxy.unwrap(container.getBeanManager()).getServices();
            ExecutorServices es = registry.get(ExecutorServices.class);
            assertNotNull(es);
            Collection<Callable<Void>> callables = new ArrayList<>();
            callables.add(() -> {
                isTCCLNull.set(Thread.currentThread().getContextClassLoader() == null);
                return null;
            });
            // callable should check if the TCCL was truly null
            es.invokeAllAndCheckForExceptions(callables);
            // now we assert it
            assertTrue(isTCCLNull.get());
        }
    }
}
