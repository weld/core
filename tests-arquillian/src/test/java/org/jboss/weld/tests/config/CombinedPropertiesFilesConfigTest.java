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
package org.jboss.weld.tests.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.config.files.DummySessionBean;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.jboss.weld.tests.util.SystemPropertiesLoader;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class CombinedPropertiesFilesConfigTest {

    private final static String JAR_DEPLOYMENT = "loader";
    private final static String WAR_DEPLOYMENT = "test";

    @Deployment(testable = false, order = 0, name = JAR_DEPLOYMENT)
    public static Archive<?> createSystemPropertiesLoaderArchive() {
        JavaArchive testDeployment = ShrinkWrap.create(BeanArchive.class).addClasses(SystemPropertiesLoader.class,
                PropertiesBuilder.class);
        PropertiesBuilder.newBuilder().set(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), "true")
                .set(ConfigurationKey.PRELOADER_THREAD_POOL_SIZE.get(), "10")
                .set(ConfigurationKey.RESOLUTION_CACHE_SIZE.get(), "500")
                .set(ConfigurationKey.RELAXED_CONSTRUCTION.get(), "true")
                .addAsSystemProperties(testDeployment);
        return testDeployment;
    }

    @Deployment(order = 1, name = WAR_DEPLOYMENT)
    public static Archive<?> createTestArchive() {

        BeanArchive ejbJar = ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(CombinedPropertiesFilesConfigTest.class));
        ejbJar.addClass(DummySessionBean.class).addAsResource(PropertiesBuilder.newBuilder()
                .set(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), "false")
                .set(ConfigurationKey.PRELOADER_THREAD_POOL_SIZE.get(), "5")
                .build(),
                "weld.properties");

        WebArchive war1 = Testable.archiveToTest(ShrinkWrap
                .create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(CombinedPropertiesFilesConfigTest.class)
                .addAsResource(PropertiesBuilder.newBuilder()
                        .set(ConfigurationKey.PRELOADER_THREAD_POOL_SIZE.get(), "5")
                        .set(ConfigurationKey.EXECUTOR_THREAD_POOL_TYPE.get(), "FIXED_TIMEOUT")
                        .build(),
                        "weld.properties"));

        WebArchive war2 = ShrinkWrap
                .create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(PropertiesBuilder.newBuilder()
                        .set(ConfigurationKey.PRELOADER_THREAD_POOL_SIZE.get(), "5")
                        .set(ConfigurationKey.RESOLUTION_CACHE_SIZE.get(), "1000")
                        .build(),
                        "weld.properties");

        return ShrinkWrap
                .create(EnterpriseArchive.class,
                        Utils.getDeploymentNameAsHash(CombinedPropertiesFilesConfigTest.class, Utils.ARCHIVE_TYPE.EAR))
                .addAsModules(ejbJar, war1, war2);
    }

    @Inject
    BeanManagerImpl beanManager;

    @Test
    @OperateOnDeployment(WAR_DEPLOYMENT)
    public void testWeldPropertiesFilePriorityInConfiguration() {
        WeldConfiguration configuration = beanManager.getServices().get(WeldConfiguration.class);
        // Multiple definitions but the same values
        assertEquals(false, configuration.getBooleanProperty(ConfigurationKey.CONCURRENT_DEPLOYMENT));
        assertEquals(Integer.valueOf(5), configuration.getIntegerProperty(ConfigurationKey.PRELOADER_THREAD_POOL_SIZE));
        // Unique values
        assertEquals("FIXED_TIMEOUT", configuration.getStringProperty(ConfigurationKey.EXECUTOR_THREAD_POOL_TYPE));
        assertEquals(Long.valueOf(1000), configuration.getLongProperty(ConfigurationKey.RESOLUTION_CACHE_SIZE));
    }

    @Test
    @OperateOnDeployment(WAR_DEPLOYMENT)
    public void testDefault() {
        WeldConfiguration configuration = beanManager.getServices().get(WeldConfiguration.class);
        // Default value
        assertEquals(ConfigurationKey.NON_PORTABLE_MODE.getDefaultValue(),
                configuration.getBooleanProperty(ConfigurationKey.NON_PORTABLE_MODE));
    }

    @Test
    @OperateOnDeployment(WAR_DEPLOYMENT)
    public void testSystemProperties() {
        // property defined only in weld.properties and not loaded as system property
        assertNull(System.getProperty(ConfigurationKey.EXECUTOR_THREAD_POOL_TYPE.get()));
        //property loaded only as system property
        assertEquals("true", System.getProperty(ConfigurationKey.RELAXED_CONSTRUCTION.get()));
    }
}
