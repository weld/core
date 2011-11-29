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
package org.jboss.weld.tests.beanManager.beanAttributes;

import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifierTypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyStereotypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.tests.util.BeanUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CreateBeanAttributesTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(Lake.class.getPackage()).addClass(BeanUtilities.class);
    }

    @Test
    public void testBeanAttributesForManagedBean() {
        AnnotatedType<Mountain> type = manager.createAnnotatedType(Mountain.class);
        BeanAttributes<Mountain> attributes = manager.createBeanAttributes(type);
        verifyTypes(attributes, Landmark.class, Object.class);
        verifyStereotypes(attributes, TundraStereotype.class);
        verifyQualifierTypes(attributes, Natural.class, Any.class);
        assertEquals(ApplicationScoped.class, attributes.getScope());
        assertEquals("mountain", attributes.getName());
        assertTrue(attributes.isAlternative());
        assertTrue(attributes.isNullable());
    }

    @Test
    public void testBeanAttributesForManagedBeanWithModifiedAnnotatedType() {
        AnnotatedType<Mountain> type = manager.createAnnotatedType(Mountain.class);
        AnnotatedType<Mountain> wrappedType = new WrappedAnnotatedType<Mountain>(type, new NamedLiteral("Mount Blanc"));
        BeanAttributes<Mountain> attributes = manager.createBeanAttributes(wrappedType);
        verifyTypes(attributes, Mountain.class, Landmark.class, Object.class);
        assertTrue(attributes.getStereotypes().isEmpty());
        verifyQualifierTypes(attributes, Named.class, Any.class, Default.class);
        assertEquals(Dependent.class, attributes.getScope());
        assertEquals("Mount Blanc", attributes.getName());
        assertFalse(attributes.isAlternative());
        assertTrue(attributes.isNullable());
    }
    
    @Test
    public void testBeanAttributesForSessionBean() {
        AnnotatedType<Lake> type = manager.createAnnotatedType(Lake.class);
        BeanAttributes<Lake> attributes = manager.createBeanAttributes(type);
        verifyTypes(attributes, LakeLocal.class, WaterBody.class, Landmark.class, Object.class);
        verifyStereotypes(attributes, TundraStereotype.class);
        verifyQualifierTypes(attributes, Natural.class, Any.class);
        assertEquals(Dependent.class, attributes.getScope());
        assertEquals("lake", attributes.getName());
        assertTrue(attributes.isAlternative());
        assertTrue(attributes.isNullable());
    }
}
