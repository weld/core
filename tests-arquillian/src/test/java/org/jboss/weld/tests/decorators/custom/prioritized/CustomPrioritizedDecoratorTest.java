/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.decorators.custom.prioritized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.ActionSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see WELD-2000
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class CustomPrioritizedDecoratorTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap
                .create(BeanArchive.class)
                .addClasses(Decorated.class, DecoratedBar.class, FooDecorator.class, CustomPrioritizedDecorator.class,
                        CustomPrioritizedDecoratorExtension.class, LowPriorityGlobalDecorator.class,
                        HighPriorityGlobalDecorator.class,
                        ActionSequence.class)
                .addAsServiceProvider(Extension.class, CustomPrioritizedDecoratorExtension.class);
    }

    @Test
    public void testCustomDecorator(DecoratedBar bar) {
        ActionSequence.reset();
        assertNotNull(bar);
        assertEquals(5, bar.foo());
        List<String> data = ActionSequence.getSequenceData();
        assertEquals(4, data.size());
        assertEquals(LowPriorityGlobalDecorator.class.getName(), data.get(0));
        assertEquals(FooDecorator.class.getName(), data.get(1));
        assertEquals(HighPriorityGlobalDecorator.class.getName(), data.get(2));
        assertEquals(DecoratedBar.class.getName(), data.get(3));
    }
}
