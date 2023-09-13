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
package org.jboss.weld.tests.instance.iterator.dependent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

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
 * Test for WELD-1320. It verifies the iterator works correctly.
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class InstanceIteratorTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InstanceIteratorTest.class))
                .addPackage(InstanceIteratorTest.class.getPackage())
                .addClasses(ActionSequence.class, Utils.class);
    }

    @Inject
    BeanManager beanManager;

    @Test
    public void testIteration() {

        ActionSequence.reset();

        Bean<Master> bean = Utils.getBean(beanManager, Master.class);
        CreationalContext<Master> ctx = beanManager.createCreationalContext(bean);
        Master master = bean.create(ctx);
        master.iterate();
        bean.destroy(master, ctx);

        ActionSequence init = ActionSequence.getSequence("init");
        assertNotNull(init);
        assertEquals(2, init.getData().size());
        assertTrue(init.containsAll(Alpha.class.getName(), Bravo.class.getName()));

        ActionSequence destroy = ActionSequence.getSequence("destroy");
        assertNotNull(destroy);
        assertEquals(2, destroy.getData().size());
        assertTrue(destroy.containsAll(Alpha.class.getName(), Bravo.class.getName()));
    }

    @Test
    public void testRemoveNotSupported() {
        Bean<Master> bean = Utils.getBean(beanManager, Master.class);
        CreationalContext<Master> ctx = beanManager.createCreationalContext(bean);
        Master master = bean.create(ctx);
        master.iterateAndRemove();
        bean.destroy(master, ctx);
    }

}
