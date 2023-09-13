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
package org.jboss.weld.tests.proxy;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ProxyTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProxyTest.class))
                .addPackage(ProxyTest.class.getPackage());
    }

    @Inject
    private BeanManagerImpl beanManager;

    /*
     * description = "WBRI-122"
     */
    @Test
    public void testImplementationClassImplementsSerializable() {
        Bean<?> bean = beanManager.resolve(beanManager.getBeans("foo"));
        Assert.assertNotNull(beanManager.getReference(bean, Object.class, beanManager.createCreationalContext(bean)));

    }

    @Test
    public void testProxyInvocations() {
        Bean<?> bean = beanManager.resolve(beanManager.getBeans("foo"));
        Foo foo = (Foo) beanManager.getReference(bean, Foo.class, beanManager.createCreationalContext(bean));
        Assert.assertEquals(Foo.MESSAGE, foo.getMsg(0, 0L, 0D, false, 'a', 0F, (short) 0));
        Assert.assertEquals(Foo.MESSAGE, foo.getRealMsg(0, 0L, 0D, false, 'a', 0F, (short) 0));
    }

    @Test
    public void testSelfInvocationInConstructor() {
        Bean<?> bean = beanManager.resolve(beanManager.getBeans("baz"));
        Baz baz = (Baz) beanManager.getReference(bean, Baz.class, beanManager.createCreationalContext(bean));
        Assert.assertEquals(1, baz.getCount());
    }

    /**
     * The proxy hashCode should be equal to the class hashCode (see WELD-695)
     */
    @Test
    public void testHashCodeImplmentation() {
        Bean<?> bean = beanManager.resolve(beanManager.getBeans("baz"));
        Baz baz = (Baz) beanManager.getReference(bean, Baz.class, beanManager.createCreationalContext(bean));
        Assert.assertTrue(baz.hashCode() == baz.getClass().hashCode());

        bean = beanManager.resolve(beanManager.getBeans("burt"));
        Burt burt = (Burt) beanManager.getReference(bean, Burt.class, beanManager.createCreationalContext(bean));
        Assert.assertTrue(burt.hashCode() == burt.getClass().hashCode());
    }

    @Test
    public void testEqualsImplmentation() {
        Bean<?> bean = beanManager.resolve(beanManager.getBeans("baz"));
        Baz baz1 = (Baz) beanManager.getReference(bean, Baz.class, beanManager.createCreationalContext(bean));
        Baz baz2 = (Baz) beanManager.getReference(bean, Baz.class, beanManager.createCreationalContext(bean));
        Assert.assertEquals(baz1, baz2);

        bean = beanManager.resolve(beanManager.getBeans("burt"));
        Burt burt1 = (Burt) beanManager.getReference(bean, Burt.class, beanManager.createCreationalContext(bean));
        Burt burt2 = (Burt) beanManager.getReference(bean, Burt.class, beanManager.createCreationalContext(bean));
        Assert.assertEquals(burt1, burt2);
    }

    @Test
    public void testBeanInstanceDoesNotEscape() {
        Bean<?> bean = beanManager.resolve(beanManager.getBeans("wobble"));
        Wobble wobble = (Wobble) beanManager.getReference(bean, Wobble.class, beanManager.createCreationalContext(bean));
        Assert.assertSame(wobble, wobble.getThis());
        // package private classes have a diffent code path
        // as they do not use direct bytecode invocation
        bean = beanManager.resolve(beanManager.getBeans("wibble"));
        Wibble wibble = (Wibble) beanManager.getReference(bean, Wibble.class, beanManager.createCreationalContext(bean));
        Assert.assertSame(wibble, wibble.getThis());
    }
}
