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
package org.jboss.weld.tests.interceptors.suicide;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

@Suicidal
@ApplicationScoped
public class InterceptedBean {

    static boolean created;
    static boolean destroyed;

    private final AtomicInteger counter = new AtomicInteger();

    public int ping(boolean valid) {
        if (!valid) {
            throw new RuntimeException("Not valid");
        }
        return counter.incrementAndGet();
    }

    @PostConstruct
    public void postConstruct() {
        created = true;
    }

    @PreDestroy
    public void preDestroy() {
        destroyed = true;
    }

    public static void reset() {
        created = false;
        destroyed = false;
    }
}
