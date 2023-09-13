/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.classDefining.a;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.jboss.weld.tests.classDefining.b.BeanInterface;
import org.jboss.weld.tests.classDefining.c.AppScopedBean;

@ApplicationScoped
public class BeanWithProducer {

    @Inject
    private AppScopedBean bean;

    private int number;
    private int number2;

    @Produces
    @ApplicationScoped
    public BeanInterface createTestInterface() {
        return x -> number = x;
    }

    @Produces
    @ApplicationScoped
    public BeanInterface.NestedInterface createNestedInterface() {
        return x -> number2 = x;
    }

    public int ping() {
        bean.passNumberToInterface();
        return number;
    }

    public int pingNested() {
        bean.passNumberToNestedInterface();
        return number2;
    }
}
