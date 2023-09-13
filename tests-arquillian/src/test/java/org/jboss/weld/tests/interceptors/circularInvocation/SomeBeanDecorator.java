/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.weld.tests.interceptors.circularInvocation;

import java.util.ArrayList;
import java.util.List;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

/**
 * @author Marius Bogoevici
 */
@Decorator
public class SomeBeanDecorator implements SomeBean {
    @Inject
    @Any
    @Delegate
    SomeBean delegate;

    public static List<String> calls = new ArrayList<String>();

    public void methodA() {
        calls.add("methodA");
        delegate.methodA();
    }

    public void methodB() {
        calls.add("methodB");
        delegate.methodB();
    }
}
