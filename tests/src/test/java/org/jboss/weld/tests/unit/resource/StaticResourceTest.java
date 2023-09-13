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
package org.jboss.weld.tests.unit.resource;

import org.jboss.arquillian.container.weld.embedded.mock.BeanDeploymentArchiveImpl;
import org.jboss.arquillian.container.weld.embedded.mock.FlatDeployment;
import org.jboss.weld.bean.builtin.ee.StaticEEResourceProducerField;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.injection.spi.InjectionServices;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Verifies, that {@link StaticEEResourceProducerField} invokes
 * {@link InjectionServices#aroundInject(org.jboss.weld.injection.spi.InjectionContext)}
 * to let it inject a static resource.
 *
 * @author Jozef Hartinger
 *
 * @see WELD-1505
 *
 */
public class StaticResourceTest {

    @Test
    public void testStaticResourceInjectionWithInjectionServices() {

        // Create the BDA in which we will deploy FooExtension. This BDA does not have access to bda2
        final BeanDeploymentArchive bda = new BeanDeploymentArchiveImpl("1", InjectedClass.class) {
            @Override
            protected void configureServices(Environment environment) {
                getServices().add(InjectionServices.class, new TestInjectionServices());
            }
        };

        // Create a deployment, that we can use to mirror the structure of one Extension inside a BDA, and one outside
        Deployment deployment = new FlatDeployment(new BeanDeploymentArchive[] { bda }) {

            @Override
            protected void configureServices(Environment environment) {
                super.configureServices(environment);
                getServices().add(TransactionServices.class, new TestTransactionServices());
            }

            @Override
            public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
                return bda;
            }
        };

        final WeldBootstrap bootstrap = new WeldBootstrap();
        bootstrap.startContainer(Environments.EE, deployment).startInitialization().deployBeans().validateBeans()
                .endInitialization();

        try {
            final BeanManagerImpl manager = bootstrap.getManager(bda);
            final SpecialResource resource = manager.instance().select(SpecialResource.class).get();
            Assert.assertNotNull(resource);
            Assert.assertEquals(resource.getName(), TestInjectionServices.RESOURCE_NAME);
        } finally {
            bootstrap.shutdown();
        }

    }
}
