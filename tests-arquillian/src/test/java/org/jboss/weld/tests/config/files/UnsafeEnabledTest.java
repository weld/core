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
package org.jboss.weld.tests.config.files;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class UnsafeEnabledTest {

    @Deployment
    public static Archive<?> createTestArchive() {

        BeanArchive ejbJar = ShrinkWrap.create(BeanArchive.class);
        ejbJar.addClass(DummySessionBean.class)
                .addAsResource(PropertiesBuilder.newBuilder().set(ConfigurationKey.RELAXED_CONSTRUCTION.get(), "true").build(), "weld.properties");

        WebArchive war1 = Testable.archiveToTest(ShrinkWrap
                .create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(UnsafeEnabledTest.class, UnproxyableBean.class));

        return ShrinkWrap.create(EnterpriseArchive.class, Utils.getDeploymentNameAsHash(UnsafeEnabledTest.class, Utils.ARCHIVE_TYPE.EAR)).addAsModules(ejbJar, war1);
    }

    @Inject
    BeanManagerImpl beanManager;

    @Inject
    UnproxyableBean unproxyable;

    @Test
    public void testConfiguration() {
        WeldConfiguration configuration = beanManager.getServices().get(WeldConfiguration.class);
        assertEquals(true, configuration.getBooleanProperty(ConfigurationKey.RELAXED_CONSTRUCTION));
        unproxyable.ping();
    }

}
