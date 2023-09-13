/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.disposer.parameter.position;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see https://issues.jboss.org/browse/WELD-1117
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class DisposedParameterPositionTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(DisposedParameterPositionTest.class))
                .addClasses(Producer.class, Product.class);
    }

    @Test
    public void testDisposedParameterIsNotFirstParameter(BeanManager manager) {
        Product.reset();

        Bean<Product> bean = Reflections.cast(manager.resolve(manager.getBeans(Product.class)));
        CreationalContext<Product> ctx = manager.createCreationalContext(bean);
        Product instance = bean.create(ctx);

        assertTrue(Product.isCreated());
        assertFalse(Product.isDisposed());

        bean.destroy(instance, ctx);

        assertTrue(Product.isCreated());
        assertTrue(Product.isDisposed());
    }
}
