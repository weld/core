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
package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.modify;

import static org.jboss.weld.tests.util.BeanUtilities.verifyQualifiers;
import static org.jboss.weld.tests.util.BeanUtilities.verifyStereotypes;
import static org.jboss.weld.tests.util.BeanUtilities.verifyTypes;
import static org.junit.Assert.assertEquals;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.BeanUtilities;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SetBeanAttributesTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SetBeanAttributesTest.class))
                .alternate(Cat.class).addPackage(Cat.class.getPackage()).addClass(BeanUtilities.class)
                .addAsServiceProvider(Extension.class, ModifyingExtension.class);
    }

    @Test
    public void testBeanModified() {
        assertEquals(0, manager.getBeans(Cat.class, Default.Literal.INSTANCE).size());
        assertEquals(0, manager.getBeans(Animal.class, Any.Literal.INSTANCE).size());
        assertEquals(0, manager.getBeans(Animal.class, new Wild.Literal(false)).size());

        assertEquals(1, manager.getBeans(Cat.class, new Wild.Literal(true)).size());
        assertEquals(1, manager.getBeans(Cat.class, new Cute.Literal()).size());
        assertEquals(1, manager.getBeans("cat").size());

        Bean<Cat> bean = Reflections.cast(manager.resolve(manager.getBeans(Cat.class, new Cute.Literal())));

        // qualifiers
        verifyQualifiers(bean, new Wild.Literal(true), new Cute.Literal(), Any.Literal.INSTANCE);
        // types
        verifyTypes(bean, Object.class, Cat.class);
        // stereotypes
        verifyStereotypes(bean, PersianStereotype.class);
        // other attributes
        assertEquals(ApplicationScoped.class, bean.getScope());
        assertEquals(true, bean.isAlternative());
    }
}
