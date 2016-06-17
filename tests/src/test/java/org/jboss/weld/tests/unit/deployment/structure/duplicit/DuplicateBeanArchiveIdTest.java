/*
 * JBoss, Home of Professional Open Source
 * Copyright 201, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.deployment.structure.duplicit;

import org.jboss.arquillian.container.weld.embedded.mock.BeanDeploymentArchiveImpl;
import org.jboss.arquillian.container.weld.embedded.mock.FlatDeployment;
import org.jboss.arquillian.container.weld.embedded.mock.TestContainer;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.exceptions.DeploymentException;
import org.testng.annotations.Test;

/**
 * https://issues.jboss.org/browse/WELD-2165
 *
 * @author Martin Kouba
 */
public class DuplicateBeanArchiveIdTest {

    @Test(expectedExceptions = { DeploymentException.class })
    public void test() {
        // Override equals/hashcode - bda is used in a map
        final BeanDeploymentArchive bda1 = new BeanDeploymentArchiveImpl("1", Foo.class) {

            @Override
            public boolean equals(Object obj) {
                return (this == obj);
            }

            @Override
            public int hashCode() {
                return System.identityHashCode(this);
            }
        };
        final BeanDeploymentArchive bda2 = new BeanDeploymentArchiveImpl("1", Bar.class) {

            @Override
            public boolean equals(Object obj) {
                return (this == obj);
            }

            @Override
            public int hashCode() {
                return System.identityHashCode(this);
            }
        };

        Deployment deployment = new FlatDeployment(new BeanDeploymentArchive[] { bda1, bda2 }) {

            @Override
            public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
                return loadBeanDeploymentArchive(beanClass);
            }
        };

        TestContainer container = new TestContainer(deployment);
        container.startContainer();
        container.stopContainer();
    }
}
