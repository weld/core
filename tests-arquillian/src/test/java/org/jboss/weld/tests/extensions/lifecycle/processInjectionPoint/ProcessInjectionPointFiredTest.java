/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.extensions.lifecycle.processInjectionPoint;

import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifierTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
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
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProcessInjectionPointFiredTest.class))
                .addPackage(Alpha.class.getPackage()).addAsServiceProvider(Extension.class, VerifyingExtension.class)
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
