/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.classDefining.inherited;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.classDefining.inherited.base.AncestorInterface;
import org.jboss.weld.tests.classDefining.inherited.extending.MyInterface;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that you can create a proxy (specifically under JDK 11) from producer that returns a hierarchical interface
 * type. E.g. tests that the resulting proxy name has package corresponding to its class name.
 *
 * One of the proxies is built on an interface extending Principal, this is deliberate as that lies in java.* package
 * which gets special treatment.
 */
@RunWith(Arquillian.class)
public class ProxyForHierarchicalInterfaceTypeTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProxyForHierarchicalInterfaceTypeTest.class))
                .addClass(AncestorInterface.class)
                .addClass(MyInterface.class)
                .addClass(ConsumerBean.class)
                .addClass(BeanProducer.class)
                .addClass(AMuchBetterPrincipal.class);
    }

    @Inject
    ConsumerBean bean;

    @Test
    public void testProxyDefinitionWorks() {
        // invoke the method, the verification lies mainly in not getting errors when creating proxy
        MyInterface interfaceBean = this.bean.getProducedInterfaceBean();
        Assert.assertEquals(MyInterface.class.getSimpleName(), interfaceBean.anotherPing());
        Assert.assertEquals(AncestorInterface.class.getSimpleName(), interfaceBean.ping());
        // assert that the proxy from hierarchical interface starts with package and class of the most specific interface we know of
        Assert.assertTrue(interfaceBean.getClass().getName()
                .startsWith("org.jboss.weld.tests.classDefining.inherited.extending.MyInterface"));

        AMuchBetterPrincipal principal = this.bean.getPrincipal();
        Assert.assertEquals(AMuchBetterPrincipal.class.getSimpleName(), principal.getName());
        // assert that the proxy created from Principal and custom class has the package of custom class
        Assert.assertTrue(
                principal.getClass().getName().startsWith("org.jboss.weld.tests.classDefining.inherited.AMuchBetterPrincipal"));
    }
}
