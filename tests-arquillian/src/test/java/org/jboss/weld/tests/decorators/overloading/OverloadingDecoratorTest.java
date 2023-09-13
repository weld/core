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
package org.jboss.weld.tests.decorators.overloading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 *
 */
@RunWith(Arquillian.class)
public class OverloadingDecoratorTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(OverloadingDecoratorTest.class))
                .decorate(AlphaServiceDecorator.class).decorate(BravoServiceDecorator.class)
                .decorate(CharlieServiceDecorator.class).addPackage(OverloadingDecoratorTest.class.getPackage())
                .addClass(ActionSequence.class);
    }

    @Test
    public void testAlpha(AlphaService service) {
        ActionSequence.reset();
        service.run(new HashSet<Integer>());
        assertEquals(2, ActionSequence.getSequenceSize());
        assertTrue(ActionSequence.getSequence().beginsWith(Set.class.getName(), Alpha.class.getName() + Set.class.getName()));
    }

    @Test
    public void testBravo(BravoService service) {
        ActionSequence.reset();
        service.run(new HashSet<Integer>());
        assertEquals(2, ActionSequence.getSequenceSize());
        assertTrue(ActionSequence.getSequence().beginsWith(Set.class.getName(), Bravo.class.getName() + Set.class.getName()));
    }

    @Test
    public void testCharlie(CharlieService<List<String>> service) {
        ActionSequence.reset();
        service.run(new HashSet<String>());
        assertEquals(2, ActionSequence.getSequenceSize());
        assertTrue(ActionSequence.getSequence().beginsWith(Set.class.getName(), Charlie.class.getName() + Set.class.getName()));
    }
}
