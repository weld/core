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
package org.jboss.weld.tests.extensions.lifecycle.processInjectionPoint;

import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifierTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.tests.util.BeanUtilities;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ProcessInjectionPointFiredTest {

    @Inject
    private VerifyingExtension extension;

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(Alpha.class.getPackage()).addAsServiceProvider(Extension.class, VerifyingExtension.class)
                .addClass(BeanUtilities.class);
    }

    @Test
    public void testFieldInjectionPoint() {
        InjectionPoint ip = extension.getAlpha();
        assertNotNull(ip);
        verifyQualifierTypes(ip.getQualifiers(), Foo.class);
        assertNotNull(ip.getBean());
        assertEquals(extension.getInjectingBean(), ip.getBean());
        verifyType(ip, Alpha.class, String.class);
        verifyAnnotated(ip);
        verifyMember(ip, InjectingBean.class);
        assertFalse(ip.isDelegate());
        assertTrue(ip.isTransient());
    }

    @Test
    public void testConstructorInjectionPoint() {
        InjectionPoint ip = extension.getBravo();
        assertNotNull(ip);
        verifyQualifierTypes(ip.getQualifiers(), Bar.class);
        assertNotNull(ip.getBean());
        assertEquals(extension.getInjectingBean(), ip.getBean());
        verifyType(ip, Bravo.class, String.class);
        verifyAnnotated(ip);
        verifyMember(ip, InjectingBean.class);
        assertFalse(ip.isDelegate());
        assertFalse(ip.isTransient());
    }

    @Test
    public void testInitializerInjectionPoint() {
        InjectionPoint ip = extension.getCharlie();
        assertNotNull(ip);
        verifyQualifierTypes(ip.getQualifiers(), Default.class);
        assertNotNull(ip.getBean());
        assertEquals(extension.getInjectingBean(), ip.getBean());
        verifyType(ip, Charlie.class);
        verifyAnnotated(ip);
        verifyMember(ip, InjectingBean.class);
        assertFalse(ip.isDelegate());
        assertFalse(ip.isTransient());
    }

    @Test
    public void testProducerMethodInjectionPoint1() {
        InjectionPoint ip = extension.getProducerAlpha();
        assertNotNull(ip);
        verifyQualifierTypes(ip.getQualifiers(), Foo.class);
        assertNotNull(ip.getBean());
        assertEquals(extension.getProducingBean(), ip.getBean());
        verifyType(ip, Alpha.class, Integer.class);
        verifyAnnotated(ip);
        verifyMember(ip, InjectingBean.class);
        assertFalse(ip.isDelegate());
        assertFalse(ip.isTransient());
    }

    @Test
    public void testProducerMethodInjectionPoint2() {
        InjectionPoint ip = extension.getProducerBravo();
        assertNotNull(ip);
        verifyQualifierTypes(ip.getQualifiers(), Bar.class);
        assertNotNull(ip.getBean());
        assertEquals(extension.getProducingBean(), ip.getBean());
        verifyType(ip, Bravo.class, Integer.class);
        verifyAnnotated(ip);
        verifyMember(ip, InjectingBean.class);
        assertFalse(ip.isDelegate());
        assertFalse(ip.isTransient());
    }

    private static void verifyType(InjectionPoint ip, Class<?> rawType, Class<?>... typeParameters) {
        assertEquals(Reflections.getRawType(ip.getType()), rawType);
        if (typeParameters.length > 0) {
            assertTrue(ip.getType() instanceof ParameterizedType);
            assertTrue(Arrays.equals(typeParameters, Reflections.getActualTypeArguments(ip.getType())));
        }
    }

    private static void verifyAnnotated(InjectionPoint ip) {
        assertNotNull(ip.getAnnotated());
        assertTrue(ip.getAnnotated().isAnnotationPresent(PlainAnnotation.class));
    }

    private static void verifyMember(InjectionPoint ip, Class<?> declaringClass) {
        assertNotNull(ip.getMember());
        assertEquals(declaringClass, ip.getMember().getDeclaringClass());
    }
}
