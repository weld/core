/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.nonportable.injectionpoint;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test that if non-portable mode enabled built-in InjectionPoint bean works correctly during AfterBeanDiscovery.
 */
@RunWith(Arquillian.class)
public class NonPortableModeInjectionPointBeanTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class)
                .addClasses(Bloom.class, Fig.class, FlowerExtension.class)
                .addAsServiceProvider(Extension.class, FlowerExtension.class).addAsResource(
                        PropertiesBuilder.newBuilder()
                                .set(ConfigurationKey.NON_PORTABLE_MODE.get(), "true").build(),
                        "weld.properties");
    }

    @Inject
    FlowerExtension ext;

    @Test
    public void testConfiguration() {
        Assert.assertEquals(Fig.class.getSimpleName(), ext.getPingResult());
    }

}
