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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.modify;

import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifiers;
import static org.jboss.weld.tests.util.BeanUtilities.verifyStereotypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyTypes;
import static org.junit.Assert.assertEquals;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.tests.util.BeanUtilities;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SetBeanAttributesTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(Cat.class.getPackage()).addClass(BeanUtilities.class)
                .addAsServiceProvider(Extension.class, ModifyingExtension.class);
    }

    @Test
    public void testBeanModified() {
        assertEquals(0, manager.getBeans(Cat.class, DefaultLiteral.INSTANCE).size());
        assertEquals(0, manager.getBeans(Animal.class, AnyLiteral.INSTANCE).size());
        assertEquals(0, manager.getBeans(Animal.class, new Wild.Literal(false)).size());

        assertEquals(1, manager.getBeans(Cat.class, new Wild.Literal(true)).size());
        assertEquals(1, manager.getBeans(Cat.class, new Cute.Literal()).size());
        assertEquals(1, manager.getBeans("cat").size());

        Bean<Cat> bean = Reflections.cast(manager.resolve(manager.getBeans(Cat.class, new Cute.Literal())));

        // qualifiers
        verifyQualifiers(bean, new Wild.Literal(true), new Cute.Literal(), AnyLiteral.INSTANCE);
        // types
        verifyTypes(bean, Object.class, Cat.class);
        // stereotypes
        verifyStereotypes(bean, PersianStereotype.class);
        // other attributes
        assertEquals(ApplicationScoped.class, bean.getScope());
        assertEquals(true, bean.isAlternative());
        assertEquals(true, bean.isNullable());
    }
}
