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
package org.jboss.weld.tests.proxy.privateconstructor;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class ProxyConstructorTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(ProxyConstructorTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(ProxyConstructorTest.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "classes/META-INF/org.jboss.weld.enableUnsafeProxies");
    }

    private Holder holder;

    @Test
    public void testProxy() {
        Assert.assertNotNull("Null holder", holder);
        Assert.assertNotNull("Null foo", holder.foo);
        Assert.assertEquals("ping", holder.foo.ping());
    }

    @Inject
    public void setHolder(Holder holder) {
        this.holder = holder;
    }
}
