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
package org.jboss.weld.tests.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.literal.NewLiteral;
import org.jboss.weld.tests.enums.EnclosingClass.AdvancedEnum;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EnumInjectionTest {

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(Cat.class.getPackage()).addAsServiceProvider(Extension.class, VerifyingExtension.class);
    }

    @Test
    public void testBasicEnum() {
        verifyBasicEnum(BasicEnum.FOO);
        verifyBasicEnum(BasicEnum.BAR);
        verifyBasicEnum(BasicEnum.BAZ);
    }

    @Test
    public void testAdvancedEnum() {
        assertEquals(2, EnclosingClass.AdvancedEnum.values().length);
        for (EnclosingClass.AdvancedEnum item : EnclosingClass.AdvancedEnum.values()) {
            assertNotNull(item.getSuperclassCat());
            assertNotNull(item.getSuperclassDog());
            assertNotNull(item.getInitializerAbstractDog());
            assertNotNull(item.getSubclassCat());
            assertNotNull(item.getSubclassDog());
        }
    }

    @Test
    public void testInstanceShared() {
        EnclosingClass.AdvancedEnum.FOO.getSubclassDog().setName("Rex");
        assertEquals("Rex", EnclosingClass.AdvancedEnum.BAR.getSuperclassDog().getName());
    }

    @Test
    public void testNewBeansCreated(BeanManager manager) {
        assertNotNull(EnumWithNewInjectionPoint.FOO.getCat());
        assertNotNull(EnumWithNewInjectionPoint.FOO.getDog());
        assertEquals(1, manager.getBeans(Cat.class, NewLiteral.DEFAULT_INSTANCE).size());
        assertEquals(1, manager.getBeans(Dog.class, NewLiteral.DEFAULT_INSTANCE).size());
    }
    
    @Test
    public void testProcessAnnotatedTypeFiredForEnums(VerifyingExtension extension) {
        assertTrue(extension.getObservedEnums().contains(BasicEnum.class));
        assertTrue(extension.getObservedEnums().contains(AdvancedEnum.class));
        assertTrue(extension.getObservedEnums().contains(EnumWithNewInjectionPoint.class));
    }

    public static void verifyBasicEnum(BasicEnum e) {
        assertNotNull(e.getCat());
        assertNotNull(e.getDog());
        assertNotNull(e.getCat().getIp());
        assertNull(e.getCat().getIp().getBean());
    }
}
