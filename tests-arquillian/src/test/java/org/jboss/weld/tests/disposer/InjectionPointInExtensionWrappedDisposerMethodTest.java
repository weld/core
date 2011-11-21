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
package org.jboss.weld.tests.disposer;

import static org.jboss.weld.util.reflection.Reflections.cast;
import static org.junit.Assert.assertNotNull;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see WELD-1018
 * 
 * @author Jozef Hartinger
 * 
 */
@RunWith(Arquillian.class)
@Ignore("WELD-1018")
public class InjectionPointInExtensionWrappedDisposerMethodTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(Car.class.getPackage())
                .addAsServiceProvider(Extension.class, SportscarFactoryExtension.class);
    }

    @Test
    public void testCallingDisposeInjectsInjectionPoint() {
        Bean<Car> bean = cast(manager.resolve(manager.getBeans(Car.class, new Sport.Literal())));
        CreationalContext<Car> ctx = manager.createCreationalContext(bean);
        Car instance = cast(manager.getReference(bean, Car.class, ctx));
        assertNotNull(instance);
        bean.destroy(instance, ctx);
    }
}
