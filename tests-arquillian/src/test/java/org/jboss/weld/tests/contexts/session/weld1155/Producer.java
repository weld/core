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
package org.jboss.weld.tests.contexts.session.weld1155;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;

@Dependent
public class Producer {

    private static final CountDownLatch latch = new CountDownLatch(2);

    @Produces
    @SessionScoped
    public static Product produceTrickyProduct(Bean<Product> bean) {
        try {
            latch.countDown();
            if (latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Producer method " + bean.toString() + " invoked twice for the same session.");
            } else {
                return new Product();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
