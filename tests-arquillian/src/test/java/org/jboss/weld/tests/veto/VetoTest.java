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
package org.jboss.weld.tests.veto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.tests.veto.package1.Hippo;
import org.jboss.weld.tests.veto.package2.Giraffe;
import org.jboss.weld.tests.veto.package2.Rhino;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class VetoTest {

    @Inject
    private BeanManager manager;

    @Inject
    private VerifyingExtension extension;

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackages(true, Elephant.class.getPackage()).addAsServiceProvider(Extension.class, VerifyingExtension.class, ModifyingExtension.class);
    }

    @Test
    public void testClassLevelVeto() {
        assertTrue(extension.getClasses().contains(Elephant.class));
        assertEquals(0, manager.getBeans(Elephant.class, AnyLiteral.INSTANCE).size());
    }
    
    @Test
    public void testPackageLevelVeto() {
        assertTrue(extension.getClasses().contains(Hippo.class));
        assertEquals(0, manager.getBeans(Hippo.class, AnyLiteral.INSTANCE).size());
    }
    
    @Test
    public void testClassLevelRequirements() {
        assertTrue(extension.getClasses().contains(Lion.class));
        assertTrue(extension.getClasses().contains(Tiger.class));
        assertEquals(1, manager.getBeans(Lion.class, AnyLiteral.INSTANCE).size());
        assertEquals(0, manager.getBeans(Tiger.class, AnyLiteral.INSTANCE).size());
    }
    
    @Test
    public void testPackageLevelRequirements() {
        assertTrue(extension.getClasses().contains(Rhino.class));
        assertTrue(extension.getClasses().contains(Giraffe.class));
        assertEquals(0, manager.getBeans(Rhino.class, AnyLiteral.INSTANCE).size());
        assertEquals(0, manager.getBeans(Giraffe.class, AnyLiteral.INSTANCE).size());
    }
    
    @Test
    public void testWithExtension() {
        assertTrue(extension.getClasses().contains(Leopard.class));
        assertEquals(1, manager.getBeans(Leopard.class, AnyLiteral.INSTANCE).size());
    }
}
