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
package org.jboss.weld.tests.contexts.passivating.serialization;

import static org.junit.Assert.assertEquals;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @see https://issues.jboss.org/browse/WELD-1076
 *
 *      Note: After CDI-141 has been reverted this test makes much less sense.
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class PassivationTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(PassivationTest.class))
                .addClasses(Logger.class, NonSerializableLogger.class, ShoppingCart.class, OutputWriter.class, Utils.class);
    }

    @Test
    public void testSerializationOfPassivationCapableComponent(BeanManager manager) throws Exception {
        Bean<ShoppingCart> bean = Reflections.cast(manager.getBeans(ShoppingCart.class).iterator().next());
        CreationalContext<ShoppingCart> cc = manager.createCreationalContext(bean);
        ShoppingCart instance = bean.create(cc);

        Utils.deserialize(Utils.serialize(instance));
        Utils.deserialize(Utils.serialize(cc));

        /*
         * Non-serializable dependent instances are destroyed on serialization of the CreationalContext. This test verifies that
         * they are destroyed eventually, not necessarily after calling destroy() of the enclosing bean.
         */
        assertEquals(3, OutputWriter.getCounter());

        bean.destroy(instance, cc);
    }
}
