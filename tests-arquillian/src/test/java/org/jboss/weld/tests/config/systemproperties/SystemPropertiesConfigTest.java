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
package org.jboss.weld.tests.config.systemproperties;

import static org.junit.Assert.assertEquals;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.jboss.weld.tests.util.SystemPropertiesLoader;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class SystemPropertiesConfigTest {

    private final static String JAR_DEPLOYMENT = "loader";
    private final static String WAR_DEPLOYMENT = "test";

    @Inject
    BeanManagerImpl beanManager;

    @Deployment(testable = false, order = 0, name = JAR_DEPLOYMENT)
    public static Archive<?> createSystemPropertiesLoaderArchive() {
        JavaArchive testDeployment = ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(SystemPropertiesConfigTest.class))
                .addClasses(SystemPropertiesLoader.class, PropertiesBuilder.class);
        PropertiesBuilder.newBuilder()
                .set(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), "false")
                .set(ConfigurationKey.INJECTABLE_REFERENCE_OPTIMIZATION.get(), "true").addAsSystemProperties(testDeployment);

        return testDeployment;
    }

    @Deployment(order = 1, name = WAR_DEPLOYMENT)
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @OperateOnDeployment(WAR_DEPLOYMENT)
    public void testPropertiesLoadedAndSetAsSystemProperties() {
        assertEquals("false", System.getProperty(ConfigurationKey.CONCURRENT_DEPLOYMENT.get()));
        assertEquals("true", System.getProperty(ConfigurationKey.INJECTABLE_REFERENCE_OPTIMIZATION.get()));
    }

    @Test
    @OperateOnDeployment(WAR_DEPLOYMENT)
    public void testPropertiesLoadedAndPropagatedToWeldConfiguration() {
        WeldConfiguration configuration = beanManager.getServices().get(WeldConfiguration.class);
        assertEquals(false, configuration.getBooleanProperty(ConfigurationKey.CONCURRENT_DEPLOYMENT));
        assertEquals(true, configuration.getBooleanProperty(ConfigurationKey.INJECTABLE_REFERENCE_OPTIMIZATION));
    }

}
