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
package org.jboss.weld.tests.extensions.lifecycle.processModule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessModule;

public class ModuleProcessingExtension implements Extension {

    private int moduleCount;

    void countEvents(@Observes ProcessModule event) {
        moduleCount++;
    }

    void processAlternatives(@Observes ProcessModule event) {
        List<Class<?>> alternatives = event.getAlternatives();
        assertEquals(1, alternatives.size());
        assertTrue(alternatives.contains(Lion.class));
        alternatives.remove(Lion.class);
        alternatives.add(Tiger.class);
        assertEquals(1, alternatives.size());
        assertTrue(alternatives.contains(Tiger.class));
    }

    void processDecorators(@Observes ProcessModule event) {
        List<Class<?>> decorators = event.getDecorators();
        assertEquals(2, decorators.size());
        assertEquals(Decorator1.class, decorators.get(0));
        assertEquals(Decorator2.class, decorators.get(1));
        // do modifications
        decorators.remove(Decorator2.class);
        decorators.add(Decorator3.class);
    }

    void processInterceptors(@Observes ProcessModule event) {
        List<Class<?>> interceptors = event.getInterceptors();
        assertEquals(2, interceptors.size());
        assertEquals(Interceptor1.class, interceptors.get(0));
        assertEquals(Interceptor2.class, interceptors.get(1));
        // do modifications
        interceptors.remove(1); // Interceptor2
        interceptors.set(0, Interceptor3.class);
        interceptors.add(1, Interceptor1.class);
    }

    public int getModuleCount() {
        return moduleCount;
    }
}
