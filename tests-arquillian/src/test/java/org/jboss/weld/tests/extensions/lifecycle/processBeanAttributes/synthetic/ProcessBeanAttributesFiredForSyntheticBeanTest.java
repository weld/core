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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.tests.util.BeanUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ProcessBeanAttributesFiredForSyntheticBeanTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).alternate(Bicycle.class).addPackage(Bicycle.class.getPackage()).addClass(BeanUtilities.class)
                .addAsServiceProvider(Extension.class, BicycleExtension.class, ModifyingExtension.class);
    }

    @Test
    public void test(BeanManager manager, BicycleExtension bicycleExtension, ModifyingExtension modifyingExtension) {
        assertTrue(bicycleExtension.isVetoed());
        assertTrue(modifyingExtension.isModified());
        Set<Bean<?>> beans = manager.getBeans(Bicycle.class, AnyLiteral.INSTANCE);
        assertEquals(1, beans.size());
        Bean<?> bean = beans.iterator().next();
        Validator.validateAfterModification(bean);
    }
}
