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
package org.jboss.weld.tests.contexts.passivating.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CustomPassivatingScopeCalledWithSerializableParametersTest {

    @Inject
    private Instance<Foo> foo;

    @Inject
    private Instance<Bar> bar;

    @Inject
    private ClusteringExtension extension;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class,
                        Utils.getDeploymentNameAsHash(CustomPassivatingScopeCalledWithSerializableParametersTest.class))
                .addPackage(CustomPassivatingScopeCalledWithSerializableParametersTest.class.getPackage())
                .addAsServiceProvider(Extension.class, ClusteringExtension.class, BarExtension.class);
    }

    @Test
    public void testWithImplicitBean() {
        ClusterContext ctx = extension.getContext();
        ctx.reset();
        Foo instance = foo.get();
        assertEquals("pong", instance.ping());
        assertTrue(ctx.isCalled());
    }

    @Test
    public void testWithExtensionProvidedBean() {
        ClusterContext ctx = extension.getContext();
        ctx.reset();
        Bar instance = bar.get();
        assertEquals("pong", instance.ping());
        assertTrue(ctx.isCalled());
    }
}
