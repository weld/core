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
package org.jboss.weld.tests.unit.bootstrap;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.weld.embedded.mock.TestContainer;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.injection.spi.InjectionServices;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InjectionServicesTest {

    @Test
    public void testInjectionOfTarget() {
        TestContainer container = new TestContainer(Foo.class, Bar.class);
        CheckableInjectionServices ijs = new CheckableInjectionServices();
        for (BeanDeploymentArchive bda : container.getDeployment().getBeanDeploymentArchives()) {
            bda.getServices().add(InjectionServices.class, ijs);
        }
        container.startContainer();
        container.ensureRequestActive();

        BeanManager manager = getBeanManager(container);

        Bean<?> bean = manager.resolve(manager.getBeans(Foo.class));
        ijs.reset();
        Foo foo = (Foo) manager.getReference(bean, Foo.class, manager.createCreationalContext(bean));

        Assert.assertTrue(ijs.isBefore());
        Assert.assertTrue(ijs.isAfter());
        Assert.assertTrue(ijs.isInjectedAfter());
        Assert.assertTrue(ijs.isInjectionTargetCorrect());
        Assert.assertEquals(ijs.getAroundInjectForFooCalled(), 1);
        Assert.assertEquals(ijs.getAroundInjectForBarCalled(), 1);

        Assert.assertNotNull(foo.getBar());
        Assert.assertEquals("hi!", foo.getMessage());

        container.stopContainer();
    }

    /**
     * Get the bean manager, assuming a flat deployment structure
     */
    public static BeanManager getBeanManager(TestContainer container) {
        return container.getBeanManager(container.getDeployment().getBeanDeploymentArchives().iterator().next());
    }

}
