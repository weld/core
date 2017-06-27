/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.tests.contexts.application.event.ear.noWebArchive;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Testcase for WFLY-3334, WELD-2401
 * @author Matej Novotny
 *
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class ApplicationContextInitializedEventFiredWithNoWebArchiveTest {

    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive lib = ShrinkWrap.create(JavaArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml").addClasses(Library.class, ApplicationContextInitializedEventFiredWithNoWebArchiveTest.class);
        JavaArchive ejb = Testable.archiveToTest(ShrinkWrap.create(JavaArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml").addClass(SessionBean.class));
        return ShrinkWrap.create(EnterpriseArchive.class).addAsModule(ejb).addAsLibrary(lib);
    }

    @Test
    public void testEjbJar() {
        Assert.assertNotNull(SessionBean.EVENT);
    }

    @Test
    public void testLibrary() {
        Assert.assertNotNull(Library.EVENT);
    }
}
