/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.se.test.beandiscovery;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.weld.environment.se.test.beandiscovery.interceptors.Decorable;
import org.jboss.weld.environment.se.test.beandiscovery.interceptors.InterceptorBindingAnnotation;

@InterceptorBindingAnnotation
@Dependent
public class Cat implements Decorable {

    @Inject
    BeanManager bm;

    public BeanManager getBeanManager() {
        return bm;
    }

    public void setBeanManager(BeanManager bm) {
        this.bm = bm;
    }

    @Override
    public void methodToBeDecorated() {
    }
}
