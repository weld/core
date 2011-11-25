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
package org.jboss.weld.tests.extensions.lifecycle.processModule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessModule;

public class ModuleProcessingExtension implements Extension {

    private int moduleCount;
    private List<AnnotatedType<?>> annotatedTypes = new ArrayList<AnnotatedType<?>>();

    void countEvents(@Observes ProcessModule event) {
        moduleCount++;
    }

    void processAnnotatedTypes(@Observes ProcessModule event) {
        for (Iterator<AnnotatedType<?>> i = event.getAnnotatedTypes(); i.hasNext();) {
            annotatedTypes.add(i.next());
        }
    }

    void processAlternatives(@Observes ProcessModule event) {
        Set<Class<?>> alternatives = event.getAlternatives();
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

    public List<AnnotatedType<?>> getAnnotatedTypes() {
        return annotatedTypes;
    }
}
