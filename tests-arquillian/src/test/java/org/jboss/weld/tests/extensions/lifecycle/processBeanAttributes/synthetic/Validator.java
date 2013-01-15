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
