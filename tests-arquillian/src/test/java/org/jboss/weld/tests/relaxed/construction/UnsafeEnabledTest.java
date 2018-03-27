/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.relaxed.construction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class UnsafeEnabledTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(UnsafeEnabledTest.class))
                .addAsResource(
                        PropertiesBuilder.newBuilder()
                                .set(ConfigurationKey.RELAXED_CONSTRUCTION.get(), "true").build(),
                        "weld.properties")
                .addClasses(UnsafeEnabledTest.class, UnproxyableBean.class,
                        SimpleInterceptor.class, Simple.class);
    }

    @Inject
    BeanManagerImpl beanManager;

    @Inject
    Instance<UnproxyableBean> instance;

    @Test
    public void testRelaxedConstruction() {
        WeldConfiguration configuration = beanManager.getServices().get(WeldConfiguration.class);
        assertEquals(true, configuration.getBooleanProperty(ConfigurationKey.RELAXED_CONSTRUCTION));
        UnproxyableBean unproxyable = instance.get();
        // Interception will not work until after the constructor has finished
        assertFalse(SimpleInterceptor.INTERCEPTED.get());
        unproxyable.ping();
        assertTrue(SimpleInterceptor.INTERCEPTED.get());
    }

}
