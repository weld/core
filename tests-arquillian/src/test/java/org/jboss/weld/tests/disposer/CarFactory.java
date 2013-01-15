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
package org.jboss.weld.tests.disposer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class CarFactory {

    @Produces
    public Car create() {
        return new Car("ordinary car");
    }

    public void dispose(@Disposes Car car, InjectionPoint ip) {
        if (car == null) {
            throw new RuntimeException("car is null");
        }
        if (ip == null) {
            throw new RuntimeException("ip is null");
        }
        if (!CarFactory.class.equals(ip.getBean().getBeanClass())) {
            throw new IllegalArgumentException("illegal ip");
        }
    }
}
