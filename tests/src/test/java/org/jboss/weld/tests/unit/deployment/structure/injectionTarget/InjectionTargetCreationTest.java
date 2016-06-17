/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.deployment.structure.injectionTarget;

import static org.testng.Assert.assertNotNull;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.arquillian.container.weld.embedded.mock.BeanDeploymentArchiveImpl;
import org.jboss.arquillian.container.weld.embedded.mock.FlatDeployment;
import org.jboss.arquillian.container.weld.embedded.mock.TestContainer;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.testng.annotations.Test;

public class InjectionTargetCreationTest {

    @Test
    public void test() {
        // Create the BDA in which we will deploy FooExtension. This BDA does not have access to bda2
        final BeanDeploymentArchive bda1 = new BeanDeploymentArchiveImpl("1", FooExtension.class);

        // Create the BDA in which we will deploy FooTarget and SimpleBean. This BDA does not have access to bda1
        final BeanDeploymentArchive bda2 = new BeanDeploymentArchiveImpl("2", SimpleBean.class, FooTarget.class);

        final FooExtension extension = new FooExtension();

        // Create a deployment, that we can use to mirror the structure of one Extension inside a BDA, and one outside
        Deployment deployment = new FlatDeployment(new BeanDeploymentArchive[] { bda1, bda2 }, extension) {

            public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
                // Return bda2 if it is Observer2. Stick anything else which this test isn't about in bda1
                if (beanClass.equals(FooExtension.class)) {
                    return bda1;
                } else {
                    return bda2;
                }
            }

            public BeanDeploymentArchive getBeanDeploymentArchive(Class<?> beanClass) {
                return loadBeanDeploymentArchive(beanClass);
            }
        };

        TestContainer container = new TestContainer(deployment);
        // Cause the container to deploy the beans etc.
        container.startContainer();

        InjectionTarget<FooTarget> target = extension.getTarget();

        CreationalContext<FooTarget> ctx = container.getBeanManager(bda2).createCreationalContext(null);
        FooTarget instance = target.produce(ctx);
        target.postConstruct(instance);
        target.inject(instance, ctx);

        /*
         * Verify that the BeanManager for BDA2 (which can see SimpleBean) is used to inject FooTarget
         */
        assertNotNull(instance.getBean());
        container.stopContainer();
    }
}
