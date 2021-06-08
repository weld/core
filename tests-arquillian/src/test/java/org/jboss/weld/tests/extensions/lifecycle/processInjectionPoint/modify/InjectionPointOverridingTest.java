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
package org.jboss.weld.tests.extensions.lifecycle.processInjectionPoint.modify;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InjectionPointOverridingTest {

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InjectionPointOverridingTest.class)).decorate(AnimalDecorator.class).addPackage(Dog.class.getPackage())
                .addAsServiceProvider(Extension.class, ModifyingExtension.class);
    }

    @Test
    public void testOverridingFieldInjectionPoint(InjectingBean bean) {
        assertTrue(bean.getDog() instanceof Hound);
    }

    @Test
    public void testDelegateInjectionPoint(@Fast Hound hound, @Lazy Dog dog) {
        assertNotNull(hound);
        assertTrue(hound.decorated());
        assertNotNull(dog);
        assertTrue(dog.decorated());
    }
}
