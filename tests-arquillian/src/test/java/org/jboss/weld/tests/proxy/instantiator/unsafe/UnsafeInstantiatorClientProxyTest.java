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
package org.jboss.weld.tests.proxy.instantiator.unsafe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class UnsafeInstantiatorClientProxyTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(UnsafeInstantiatorClientProxyTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(UnsafeInstantiatorClientProxyTest.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "classes/META-INF/org.jboss.weld.enableUnsafeProxies")
                .addAsManifestResource(EmptyAsset.INSTANCE, "org.jboss.weld.enableUnsafeProxies")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testNoConstructorForProxy(DependentBar bar) {
        assertNotNull(bar);
        // The proxy is not null
        assertNotNull(bar.getFoo());
        // Verify the proxy delegates method invocation to the underlying bean instance
        String className = bar.getFoo().getClassName();
        assertNotNull(className);
        assertEquals(NormalScopedFoo.class.getName() + "$Proxy$_$$_WeldSubclass", className);
        // Verify bean constructor and @PostConstruct callback are called
        String id = bar.getFoo().ping();
        assertNotNull(id);
        assertEquals(AlphaInterceptor.MARKER + FooDecorator1.MARKER + FooDecorator2.MARKER + "Et voila!", id);
    }

    @Test
    public void testPrivateConstructorForProxy(DependentBar bar) {
        assertNotNull(bar);
        assertNotNull(bar.getBaz());
        assertEquals("baz", bar.getBaz().ping());
    }
}
