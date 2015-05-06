/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.se.test.context.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-1575
 */
@RunWith(Arquillian.class)
public class BoundSessionContextTest {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(BoundSessionContextTest.class.getPackage());
    }

    @Test
    public void testConcurrentCalls(BoundSessionContext boundSessionContext, Product product) throws InterruptedException,
            ExecutionException {

        Map<String, Object> storage = Collections.synchronizedMap(new HashMap<String, Object>());

        List<Callable<Void>> calls = new ArrayList<Callable<Void>>();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            calls.add(new ConcurrentCall(storage, boundSessionContext, product));
        }

        for (Future<Void> result : executor.invokeAll(calls)) {
            result.get();
        }
    }

    @After
    public void shutdown() {
        executor.shutdownNow();
    }

    private class ConcurrentCall implements Callable<Void> {

        private final Map<String, Object> storage;

        private final BoundSessionContext sessionContext;

        private final Product product;

        public ConcurrentCall(Map<String, Object> storage, BoundSessionContext sessionContext, Product product) {
            this.storage = storage;
            this.sessionContext = sessionContext;
            this.product = product;
        }

        public Void call() throws Exception {
            for (int i = 0; i < 1000; i++) {
                doWork();
            }
            return null;
        }

        private void doWork() {
            sessionContext.associate(storage);
            sessionContext.activate();
            // store some additional key-value pairs in the storage map to make it more likely to reproduce WELD-1932
            storage.put(ConcurrentCall.class.getName(), this);

            try {
                // Invoke the proxy - force bean instance creation
                product.ping();

                sessionContext.invalidate();
                sessionContext.deactivate();
            } finally {
                sessionContext.dissociate(storage);
                storage.remove(ConcurrentCall.class.getName());
            }
        }
    }

}
