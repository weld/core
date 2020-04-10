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
package org.jboss.weld.tests.config.integrator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.EmbeddedContainer;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@Category(EmbeddedContainer.class)
@RunWith(Arquillian.class)
public class ExternalConfigurationConfigTest {

    @Inject
    private BeanManagerImpl manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ExternalConfigurationConfigTest.class)).addClass(MyExternalConfiguration.class)
                .addAsServiceProvider(Service.class, MyExternalConfiguration.class);
    }

    @Test
    public void testBootstrapConfiguration() {
        WeldConfiguration configuration = manager.getServices().get(WeldConfiguration.class);
        assertFalse(configuration.getBooleanProperty(ConfigurationKey.CONCURRENT_DEPLOYMENT));
        assertEquals(Integer.valueOf(200), configuration.getIntegerProperty(ConfigurationKey.PRELOADER_THREAD_POOL_SIZE));
        assertEquals("/home/weld", configuration.getStringProperty(ConfigurationKey.PROXY_DUMP));
    }

}
