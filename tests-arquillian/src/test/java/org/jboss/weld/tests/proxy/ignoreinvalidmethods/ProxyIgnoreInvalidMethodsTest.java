/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.proxy.ignoreinvalidmethods;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 *
 */
@RunWith(Arquillian.class)
public class ProxyIgnoreInvalidMethodsTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProxyIgnoreInvalidMethodsTest.class))
                .addPackage(ProxyIgnoreInvalidMethodsTest.class.getPackage()).addClasses(Utils.class, ActionSequence.class)
                .addAsResource(PropertiesBuilder.newBuilder()
                        .set(ConfigurationKey.PROXY_IGNORE_FINAL_METHODS.get(),
                                HashMap.class.getName() + "|" + Alpha.class.getName())
                        .build(), "weld.properties");
    }

    @Test
    public void testHashMapProxy(@Juicy HashMap<String, String> map) {
        assertNotNull(map);
        map.put("foo", "bar");
        assertTrue(map.containsKey("foo"));
    }

    @Test
    public void testAlphaProxy(Alpha alpha) {
        assertNotNull(alpha);
        ActionSequence.reset();
        assertEquals("1", alpha.ping());
        ActionSequence.assertSequenceDataEquals(SecureInterceptor.class.getName());
        ActionSequence.reset();
        // We are able to call the method but it is not invoked on the contextual instance!
        assertNull(alpha.pong());
        assertEquals(0, ActionSequence.getSequenceSize());
    }

}
