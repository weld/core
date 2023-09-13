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
package org.jboss.weld.tests.specialization.modular;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test for specializing {@link Alternative}. Verifies that a bean is only specialized in the BDA where the specializing
 * alternative is enabled.
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class Specialization02Test {

    @Inject
    private InjectedBean1 bean1;

    @Inject
    private InjectedBean2 bean2;

    @Inject
    private Event<FactoryEvent> event;

    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive jar = ShrinkWrap.create(BeanArchive.class).alternate(AlternativeSpecializedFactory.class)
                .addClasses(Factory.class, AlternativeSpecializedFactory.class, Product.class, InjectedBean2.class,
                        FactoryEvent.class);
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(Specialization02Test.class, Utils.ARCHIVE_TYPE.WAR))
                .addClass(InjectedBean1.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addAsLibrary(jar);
    }

    @Test
    public void testEnabledAlternativeSpecializes() {
        assertTrue(bean1.getFactory().isUnsatisfied());
        assertTrue(bean1.getProduct().isUnsatisfied());
        assertFalse(bean1.getProduct().isAmbiguous());

        assertTrue(bean2.getFactory().get() instanceof AlternativeSpecializedFactory);
        assertTrue(bean2.getProduct().isUnsatisfied());
    }

    @Test
    public void testEvent() {
        Factory.reset();
        event.fire(new FactoryEvent());
        assertFalse(Factory.isEventDelivered());
    }
}
