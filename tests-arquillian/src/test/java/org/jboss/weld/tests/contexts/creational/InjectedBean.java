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
package org.jboss.weld.tests.contexts.creational;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@SuppressWarnings("unused")
@Dependent
public class InjectedBean {

    @Inject
    // not retained within CreationalContext
    private DependencyWithoutPreDestroy dependency1;

    @Inject
    // retained within CreationalContext - because it has @PreDestroy
    private DependencyWithPreDestroy dependency2;

    @Inject
    // not retained within CreationalContext
    private ProductWithoutDisposer dependency3;

    @Inject
    // retained within CreationalContext - because it has disposer method
    private ProductWithDisposer dependency4;

    @Inject
    // not retained within CreationalContext
    private Alpha dependency5;

    @Inject
    // retained within CreationalContext - because its dependency has @PreDestroy
    private Bravo dependency6;

    @Inject
    // not retained within CreationalContext
    private Charlie dependency7;

    @Inject
    // retained within CreationalContext - because its dependency has disposer method
    private Delta dependency8;

    @Inject
    @Juicy
    // retained within CreationalContext - because its dependency has @PreDestroy
    String id;
}
