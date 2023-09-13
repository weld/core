/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.cluster;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.arquillian.container.weld.embedded.mock.TestContainer;
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.cluster.AbstractClusterTest;
import org.jboss.weld.mock.cluster.SwitchableSingletonProvider;
import org.testng.annotations.Test;

public class SwitchableContainerTest extends AbstractClusterTest {

    @Test
    public void test() {

        // Bootstrap container 1
        SwitchableSingletonProvider.use(1);

        TestContainer container1 = new TestContainer(Foo.class);
        container1.startContainer();
        container1.ensureRequestActive();

        BeanManagerImpl beanManager1 = getBeanManager(container1);
        Bean<?> fooBean1 = beanManager1.resolve(beanManager1.getBeans(Foo.class));
        Foo foo1 = (Foo) beanManager1.getReference(fooBean1, Foo.class, beanManager1.createCreationalContext(fooBean1));
        foo1.setName("container 1");

        // Bootstrap container 2
        SwitchableSingletonProvider.use(2);

        TestContainer container2 = new TestContainer(Foo.class);
        container2.startContainer();
        container2.ensureRequestActive();

        BeanManagerImpl beanManager2 = getBeanManager(container2);
        Bean<?> fooBean2 = beanManager2.resolve(beanManager2.getBeans(Foo.class));
        Foo foo2 = (Foo) beanManager2.getReference(fooBean2, Foo.class, beanManager2.createCreationalContext(fooBean2));
        foo2.setName("container 2");

        // Switch to container 1 and check value
        SwitchableSingletonProvider.use(1);
        foo1 = (Foo) beanManager1.getReference(fooBean1, Foo.class, beanManager1.createCreationalContext(fooBean1));
        assert foo1.getName().equals("container 1");

        // Switch to container 2 and check value
        SwitchableSingletonProvider.use(2);
        foo2 = (Foo) beanManager2.getReference(fooBean2, Foo.class, beanManager2.createCreationalContext(fooBean2));
        assert foo2.getName().equals("container 2");
        SwitchableSingletonProvider.use(1);
        container1.stopContainer();
        SwitchableSingletonProvider.use(2);
        container2.stopContainer();
        SingletonProvider.reset();
    }

}
