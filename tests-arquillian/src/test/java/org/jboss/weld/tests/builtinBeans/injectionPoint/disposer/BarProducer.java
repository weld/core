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
package org.jboss.weld.tests.builtinBeans.injectionPoint.disposer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class BarProducer {

    private static boolean disposerCalled;

    @Produces
    public Bar produceBar() {
        return new Bar(true);
    }

    public void disposeBar(@Disposes Bar bar, InjectionPoint injectionPoint) {
        assertNotNull(injectionPoint);
        assertNotNull(injectionPoint.getBean());
        assertEquals(Foo.class, injectionPoint.getBean().getBeanClass());
        disposerCalled = true;
    }

    public static boolean isDisposerCalled() {
        return disposerCalled;
    }

    public static void reset() {
        BarProducer.disposerCalled = false;
    }

}
