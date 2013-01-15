/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.beanManager.bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

@Vetoed
@Large
@Lifecycle
public class Office implements Building<String> {

    @Inject
    private Employee fieldEmployee;

    private Employee constructorEmployee;
    private Employee initializerEmployee;
    private boolean postConstructCalled;
    private static boolean preDestroyCalled;

    @Inject
    public Office(Employee constructorEmployee) {
        this.constructorEmployee = constructorEmployee;
    }

    @Inject
    public void init(Employee employee) {
        this.initializerEmployee = employee;
    }

    @PostConstruct
    public void postConstruct() {
        postConstructCalled = true;
    }

    @PreDestroy
    public void preDestroy() {
        preDestroyCalled = true;
    }

    public Employee getFieldEmployee() {
        return fieldEmployee;
    }

    public Employee getConstructorEmployee() {
        return constructorEmployee;
    }

    public Employee getInitializerEmployee() {
        return initializerEmployee;
    }

    public boolean isPostConstructCalled() {
        return postConstructCalled;
    }

    public static boolean isPreDestroyCalled() {
        return preDestroyCalled;
    }

    @Simple
    public boolean intercepted() {
        return false;
    }
}
