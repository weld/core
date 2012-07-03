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
package org.jboss.weld.tests.extensions.lifecycle.processInjectionPoint.modify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.literal.NewLiteral;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InjectionPointOverridingTest {

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).decorate(AnimalDecorator.class).addPackage(Dog.class.getPackage())
                .addAsServiceProvider(Extension.class, ModifyingExtension.class);
    }

    @Test
    public void testOverridingFieldInjectionPoint(InjectingBean bean) {
        assertTrue(bean.getDog() instanceof Hound);
    }

    @Test
    public void testDelegateInjectionPoint(@Fast Hound hound, @Lazy Dog dog) {
        assertNotNull(hound);
        assertTrue(hound.decorated());
        assertNotNull(dog);
        assertTrue(dog.decorated());
    }

    @Test
    public void testNewInjectionPointDiscovered(InjectingBean bean, BeanManager manager) {
        assertEquals(1, manager.getBeans(Cat.class, NewLiteral.DEFAULT_INSTANCE).size());
        assertNotNull(bean.getCat());
        assertNotNull(bean.getCat().getBean());
        assertEquals(Dependent.class, bean.getCat().getBean().getScope());
        assertEquals(null, bean.getCat().getBean().getName());
    }
}
