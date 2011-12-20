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
package org.jboss.weld.tests.beanManager.injectionPoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.tests.util.BeanUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CreateInjectionPointTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(Book.class.getPackage()).addClass(BeanUtilities.class);
    }

    @Test
    public void testField() {
        AnnotatedType<?> type = manager.createAnnotatedType(Library.class);
        assertEquals(1, type.getFields().size());
        AnnotatedField<?> field = type.getFields().iterator().next();
        InjectionPoint ip = manager.createInjectionPoint(field);
        validateParameterizedType(ip.getType(), Book.class, String.class);
        BeanUtilities.verifyQualifierTypes(ip.getQualifiers(), Monograph.class, Fictional.class);
        assertNull(ip.getBean());
        assertEquals(field.getJavaMember(), ip.getMember());
        assertNotNull(ip.getAnnotated());
        assertFalse(ip.isDelegate());
        assertTrue(ip.isTransient());
    }

    @Test
    public void testConstructorParameter() {
        AnnotatedType<?> type = manager.createAnnotatedType(Library.class);
        assertEquals(1, type.getConstructors().size());
        AnnotatedConstructor<?> constructor = type.getConstructors().iterator().next();
        AnnotatedParameter<?> parameter = constructor.getParameters().get(1);
        InjectionPoint ip = manager.createInjectionPoint(parameter);
        validateParameterizedType(ip.getType(), Book.class, String.class);
        BeanUtilities.verifyQualifierTypes(ip.getQualifiers(), Fictional.class);
        assertNull(ip.getBean());
        assertEquals(constructor.getJavaMember(), ip.getMember());
        assertNotNull(ip.getAnnotated());
        assertFalse(ip.isDelegate());
        assertFalse(ip.isTransient());
    }

    @Test
    public void testMethodParameter() {
        AnnotatedType<?> type = manager.createAnnotatedType(Library.class);
        assertEquals(1, type.getMethods().size());
        AnnotatedMethod<?> method = type.getMethods().iterator().next();
        AnnotatedParameter<?> parameter = method.getParameters().get(2);
        InjectionPoint ip = manager.createInjectionPoint(parameter);
        validateParameterizedType(ip.getType(), Book.class, Integer.class);
        BeanUtilities.verifyQualifierTypes(ip.getQualifiers(), Default.class);
        assertNull(ip.getBean());
        assertEquals(method.getJavaMember(), ip.getMember());
        assertNotNull(ip.getAnnotated());
        assertFalse(ip.isDelegate());
        assertFalse(ip.isTransient());
    }

    private static void validateParameterizedType(Type type, Class<?> rawType, Type... types) {
        assertTrue(type instanceof ParameterizedType);
        ParameterizedType parameterized = (ParameterizedType) type;
        assertEquals(rawType, parameterized.getRawType());
        assertTrue(Arrays.equals(types, parameterized.getActualTypeArguments()));
    }
}
