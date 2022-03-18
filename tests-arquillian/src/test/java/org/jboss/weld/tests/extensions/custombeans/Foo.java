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
package org.jboss.weld.tests.extensions.custombeans;

import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Model;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

@Model
public class Foo implements VetoedBean {

    private static AtomicLong idGenerator = new AtomicLong(0);

    private Long id;

    public Foo() {
    }

    public Foo(Long id) {
        this.id = id;
    }

    @Inject
    BeanManager beanManager;

    @PostConstruct
    public void postConstruct() {
        id = idGenerator.incrementAndGet();
    }

    public void ping() {
        beanManager.getEvent().select(Foo.class).fire(new Foo());
    }

    public Long getId() {
        return id;
    }

}
