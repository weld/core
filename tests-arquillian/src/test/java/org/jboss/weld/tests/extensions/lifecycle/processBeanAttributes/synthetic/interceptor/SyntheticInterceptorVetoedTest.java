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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.synthetic.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InterceptionType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SyntheticInterceptorVetoedTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).intercept(ExternalInterceptor.class).addPackage(ExternalInterceptor.class.getPackage())
                .addAsServiceProvider(Extension.class, ExternalInterceptorExtension.class);
    }

    @Test
    public void testSyntheticInterceptorBeanCanBeVetoed(ExternalInterceptorExtension extension, BeanManager manager) {
        assertTrue(extension.isTypeVetoed());
        assertTrue(extension.isBeanRegistered());
        assertTrue(extension.isBeanVetoed());
        // verify that one of these is vetoed (we do not know which one)
        int fooInterceptors = manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, FooBinding.Literal.INSTANCE).size();
        int barInterceptors = manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, BarBinding.Literal.INSTANCE).size();
        assertEquals(1, fooInterceptors + barInterceptors);
    }
}
