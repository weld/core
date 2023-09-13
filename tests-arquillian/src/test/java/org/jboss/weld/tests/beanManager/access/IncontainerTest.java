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
package org.jboss.weld.tests.beanManager.access;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class IncontainerTest {

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(IncontainerTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(Alpha.class, MarkerObtainer1.class, Foo.class, Marker.class)
                .addAsWebInfResource(new BeansXml(BeanDiscoveryMode.ALL).alternatives(Alpha.class), "beans.xml");
        JavaArchive bda1 = ShrinkWrap.create(JavaArchive.class).addClasses(Bravo.class, MarkerObtainer2.class, Bar.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL).alternatives(Bravo.class), "beans.xml");
        JavaArchive bda2 = ShrinkWrap.create(JavaArchive.class).addClasses(Charlie.class, MarkerObtainer3.class, Baz.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL).alternatives(Charlie.class), "beans.xml");
        JavaArchive nonBda = ShrinkWrap.create(JavaArchive.class).addClasses(MarkerObtainer4.class);
        return war.addAsLibraries(bda1, bda2, nonBda);
    }

    @Test
    public void testCallingCdiFromBda() {
        // war itself
        assertEquals(1, MarkerObtainer1.getBeans(Marker.class).size());
        assertEquals(Alpha.class, MarkerObtainer1.getBeans(Marker.class).iterator().next().getBeanClass());
        assertEquals(1, MarkerObtainer1.getBeans(Foo.class).size());
        // bda 1
        assertEquals(1, MarkerObtainer2.getBeans(Marker.class).size());
        assertEquals(Bravo.class, MarkerObtainer2.getBeans(Marker.class).iterator().next().getBeanClass());
        assertEquals(1, MarkerObtainer2.getBeans(Bar.class).size());
        // bda 2
        assertEquals(1, MarkerObtainer3.getBeans(Marker.class).size());
        assertEquals(Charlie.class, MarkerObtainer3.getBeans(Marker.class).iterator().next().getBeanClass());
        assertEquals(1, MarkerObtainer3.getBeans(Baz.class).size());
    }

    @Test
    public void testCallingFromOutsideOfBda() {
        assertEquals(1, MarkerObtainer4.getBeans(Marker.class).size()); // because we get the root manager, which is the manager of WEB-INF/classes in this case
        assertEquals(1, MarkerObtainer4.getBeans(Foo.class).size());
        assertEquals(1, MarkerObtainer4.getBeans(Bar.class).size());
        assertEquals(1, MarkerObtainer4.getBeans(Baz.class).size());
    }

}
