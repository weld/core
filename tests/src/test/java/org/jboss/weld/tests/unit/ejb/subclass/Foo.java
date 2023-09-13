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
package org.jboss.weld.tests.unit.ejb.subclass;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

public class Foo {

    private final BeanManager manager;

    public Foo() {
        this.manager = null;
    }

    @Inject
    @BarInterceptorBinding
    public Foo(BeanManager manager) {
        this.manager = manager;
    }

    public BeanManager getManager() {
        return manager;
    }

    public int ping() {
        return 0;
    }
}
