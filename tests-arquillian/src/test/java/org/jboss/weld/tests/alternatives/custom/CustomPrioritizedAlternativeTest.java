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
package org.jboss.weld.tests.alternatives.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see WELD-2000
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class CustomPrioritizedAlternativeTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(CustomPrioritizedAlternativeTest.class))
                .addClasses(CustomPrioritizedAlternativeFoo.class, CustomPrioritizedAlternativeExtension.class, Bar.class,
                        Bla.class, Foo.class)
                .addAsServiceProvider(Extension.class, CustomPrioritizedAlternativeExtension.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCustomAlternative(BeanManager beanManager) {
        Bean<Bla> blaBean = (Bean<Bla>) beanManager.resolve(beanManager.getBeans(Bla.class));
        assertNotNull(blaBean);
        CreationalContext<Bla> ctx = beanManager.createCreationalContext(blaBean);
        Bla bla = blaBean.create(ctx);
        assertEquals(10, bla.getId());
        blaBean.destroy(bla, ctx);
    }
}
