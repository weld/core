/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.beanManager.access;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class IncontainerTest {

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class).addClasses(Alpha.class, MarkerObtainer1.class, Foo.class, Marker.class)
                .addAsWebInfResource(new BeansXml().alternatives(Alpha.class), "beans.xml");
        JavaArchive bda1 = ShrinkWrap.create(JavaArchive.class).addClasses(Bravo.class, MarkerObtainer2.class, Bar.class)
                .addAsManifestResource(new BeansXml().alternatives(Bravo.class), "beans.xml");
        JavaArchive bda2 = ShrinkWrap.create(JavaArchive.class).addClasses(Charlie.class, MarkerObtainer3.class, Baz.class)
                .addAsManifestResource(new BeansXml().alternatives(Charlie.class), "beans.xml");
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
        assertEquals(0, MarkerObtainer4.getBeans(Marker.class).size());
        assertEquals(1, MarkerObtainer4.getBeans(Foo.class).size());
        assertEquals(1, MarkerObtainer4.getBeans(Bar.class).size());
        assertEquals(1, MarkerObtainer4.getBeans(Baz.class).size());
    }

}
