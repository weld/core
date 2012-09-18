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
package org.jboss.weld.tests.builtinBeans.metadata.passivation;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

@Vetoed
@SessionScoped
public class Car implements Serializable, Vehicle {

    private static final long serialVersionUID = 3427063499096037105L;

    @Inject
    private Bean<Car> bean;

    public Bean<Car> getBean() {
        return bean;
    }

    @Fast
    public FastInterceptor getInterceptor() {
        // the interceptor uses this method to return itself for inspection
        return null;
    }

    @Override
    public VehicleDecorator getDecoratorInstance() {
        // the decorator uses this method to return itself for inspection
        return null;
    }
}
