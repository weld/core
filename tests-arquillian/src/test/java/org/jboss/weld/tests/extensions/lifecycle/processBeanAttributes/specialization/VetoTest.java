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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.specialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifiers;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.tests.util.BeanUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class VetoTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addClasses(Foo.class, Bar.class, Baz.class, Alpha.class, Bravo.class, Charlie.class, VetoingExtension.class)
                .addClass(BeanUtilities.class).addAsServiceProvider(Extension.class, VetoingExtension.class);
    }

    @Test
    public void testSpecializedBeanAvailableAfterSpecializingBeanVetoed(BeanManager manager, @Any Alpha alpha) {
        Bean<?> bean = manager.resolve(manager.getBeans(Alpha.class, AnyLiteral.INSTANCE));
        assertNotNull(bean);
        assertEquals(Bravo.class, bean.getBeanClass());
        assertEquals("alpha", bean.getName());
        verifyQualifiers(bean, Foo.Literal.INSTANCE, Bar.Literal.INSTANCE, new NamedLiteral("alpha"), AnyLiteral.INSTANCE);

        assertNotNull(alpha);
        assertTrue(alpha instanceof Bravo);
        assertFalse(alpha instanceof Charlie);
    }
}
