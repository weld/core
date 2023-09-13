/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.proxy.instantiator.unsafe;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

@RequestScoped
public class NormalScopedFoo implements NormalScopedFooInterface {

    private String id = "Et";

    @Inject
    public NormalScopedFoo(BeanManager beanManager) {
        id += " voila";
    }

    @PostConstruct
    public void init() {
        id += "!";
    }

    @AlphaBinding
    public String ping() {
        return id;
    }

    public String getClassName() {
        return this.getClass().getName();
    }
}
