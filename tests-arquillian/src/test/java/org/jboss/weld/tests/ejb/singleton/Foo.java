/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.ejb.singleton;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

@Singleton
@Startup
@ApplicationScoped
public class Foo {

    private static AtomicInteger countOfPostConstructCalled = new AtomicInteger();
    private static AtomicInteger countOfConstructorCalled = new AtomicInteger();

    public static boolean isPostConstructCalled() {
        return countOfPostConstructCalled.get() > 0;
    }

    public Foo() {
        Foo.countOfConstructorCalled.incrementAndGet();
    }

    public static void reset() {
        countOfPostConstructCalled.set(0);
        countOfConstructorCalled.set(0);
    }

    @PostConstruct
    public void postConstruct() {
        countOfPostConstructCalled.incrementAndGet();
    }

    public static int getCountOfPostConstructCalled() {
        return countOfPostConstructCalled.get();
    }

    public static int getCountOfConstructorCalled() {
        return countOfConstructorCalled.get();
    }

    public boolean getSomeValue() {
        return true;
    }

}
