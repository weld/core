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
package org.jboss.weld.tests.decorators.ordering.global;

import static org.junit.Assert.assertEquals;

import java.util.List;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.Extension;

public class DecoratorRegisteringExtension implements Extension {

    void alterDecoratorEnablement(@Observes AfterTypeDiscovery event) {
        List<Class<?>> decorators = event.getDecorators();

        // there may be decorators enabled globally by the application server
        // therefore, we use the offset to work with the part of the list that contains this applications' decorators
        int offset = getOffset(decorators);

        assertEquals(GloballyEnabledDecorator1.class.getName(), decorators.get(0 + offset).getName());
        assertEquals(GloballyEnabledDecorator2.class.getName(), decorators.get(1 + offset).getName());
        assertEquals(GloballyEnabledDecorator3.class.getName(), decorators.get(2 + offset).getName());
        assertEquals(WebApplicationGlobalDecorator.class.getName(), decorators.get(3 + offset).getName());
        assertEquals(GloballyEnabledDecorator4.class.getName(), decorators.get(4 + offset).getName());
        assertEquals(GloballyEnabledDecorator5.class.getName(), decorators.get(5 + offset).getName());

        // swap decorator2 and decorator4
        Class<?> decorator2 = decorators.get(1 + offset);
        Class<?> decorator4 = decorators.get(4 + offset);
        decorators.set(1 + offset, decorator4);
        decorators.set(4 + offset, decorator2);

        // add ExtensionEnabledDecorator1 and ExtensionEnabledDecorator2
        decorators.add(1 + offset, ExtensionEnabledDecorator1.class); // this causes all the following decorators to shift in the list
        decorators.add(6 + offset, ExtensionEnabledDecorator2.class);
    }

    private int getOffset(List<Class<?>> decorators) {
        for (int i = 0; i < decorators.size(); i++) {
            if (decorators.get(i).getName().equals(GloballyEnabledDecorator1.class.getName())) {
                return i;
            }
        }
        throw new IllegalStateException(
                "Expected decorator " + GloballyEnabledDecorator1.class.getName() + " not found within " + decorators);
    }
}
