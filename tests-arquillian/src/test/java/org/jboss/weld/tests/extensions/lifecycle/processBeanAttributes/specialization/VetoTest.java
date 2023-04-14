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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.specialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifiers;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.literal.NamedLiteral;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.BeanUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class VetoTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(VetoTest.class))
                .addClasses(Foo.class, Bar.class, Baz.class, Alpha.class, Bravo.class, Charlie.class, VetoingExtension.class, VerifyingExtension.class)
                .addClass(BeanUtilities.class).addAsServiceProvider(Extension.class, VetoingExtension.class, VerifyingExtension.class);
    }

    @Test
    public void testSpecializedBeanAvailableAfterSpecializingBeanVetoed(BeanManager manager, @Any Alpha alpha, VerifyingExtension extension) {
        Bean<?> bean = manager.resolve(manager.getBeans(Alpha.class, Any.Literal.INSTANCE));
        assertNotNull(bean);
        assertEquals(Bravo.class, bean.getBeanClass());
        assertEquals("alpha", bean.getName());
        verifyQualifiers(bean, Foo.Literal.INSTANCE, Bar.Literal.INSTANCE, new NamedLiteral("alpha"), Any.Literal.INSTANCE);

        assertNotNull(alpha);
        assertTrue(alpha instanceof Bravo);
        assertFalse(alpha instanceof Charlie);
        assertNull(extension.getAlpha());
        assertNotNull(extension.getBravo());
        assertNotNull(extension.getCharlie());

        // verify that PIT<Bravo> is only fired once even during specialization, see WELD-2742
        assertEquals(1, extension.getBravoPitInvocations());
    }
}
