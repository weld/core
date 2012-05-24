/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.beanManager.bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Veto;
import javax.inject.Inject;

@Veto
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
