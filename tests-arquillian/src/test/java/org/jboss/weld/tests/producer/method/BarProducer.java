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
package org.jboss.weld.tests.producer.method;

import java.lang.reflect.Member;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

/**
 * Class with a producer method and disposal method both containing InjectionPoint
 * parameters.
 *
 * @author David Allen
 */
@Dependent
public class BarProducer {
    private static Bar disposedBar;
    private static Member producedInjection;

    @Produces
    public Bar getBar(InjectionPoint injectionPoint) {
        producedInjection = injectionPoint.getMember();
        return new Bar("blah");
    }

    public void dispose(@Disposes @Any Bar bar) {
        disposedBar = bar;
    }

    public static Bar getDisposedBar() {
        return disposedBar;
    }

    public static Member getProducedInjection() {
        return producedInjection;
    }

    public static void reset() {
        disposedBar = null;
        producedInjection = null;
    }
}
