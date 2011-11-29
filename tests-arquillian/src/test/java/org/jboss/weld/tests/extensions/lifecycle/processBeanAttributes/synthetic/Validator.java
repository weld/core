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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.synthetic;

import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifierTypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyStereotypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyTypes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanAttributes;

public class Validator {

    public static void validateBeforeModification(BeanAttributes<?> attributes) {
        assertEquals(ApplicationScoped.class, attributes.getScope());
        verifyStereotypes(attributes, FooStereotype.class);
        verifyTypes(attributes, Object.class, Vehicle.class, Bicycle.class);
        verifyQualifierTypes(attributes, FooQualifier.class, Any.class);
        assertFalse(attributes.isAlternative());
        assertTrue(attributes.isNullable());
    }
    
    public static void validateAfterModification(BeanAttributes<?> attributes) {
        assertEquals(RequestScoped.class, attributes.getScope());
        verifyStereotypes(attributes, BarStereotype.class);
        verifyTypes(attributes, Object.class, Bicycle.class);
        verifyQualifierTypes(attributes, FooQualifier.class, Any.class);
        assertTrue(attributes.isAlternative());
        assertTrue(attributes.isNullable());
    }
}
