/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.accessibility;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class WarAccessibilityTest {

    @Inject
    private War1Impl webinfClassesImplementation;

    @Deployment
    public static Archive<?> getDeployment() {
        JavaArchive sharedInterfaceBundle = ShrinkWrap.create(JavaArchive.class).addClass(Animal.class);
        JavaArchive extensionLibrary1 = createSimpleExtensionArchive(SharedLibrary1Extension.class, SharedLibrary1Impl.class);
        JavaArchive library1 = ShrinkWrap.create(BeanArchive.class).addClass(Library1Impl.class);
        WebArchive war = ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(WarAccessibilityTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addAsLibraries(sharedInterfaceBundle, extensionLibrary1, library1)
                .addClass(War1Impl.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return war;
    }

    protected static JavaArchive createSimpleExtensionArchive(Class<? extends Extension> extension, Class<?>... classes) {
        return ShrinkWrap.create(JavaArchive.class).addClass(extension).addClasses(classes)
                .addAsServiceProvider(Extension.class, extension);
    }

    @Test
    public void testAccessibility() {
        BeanManager webinfClassesBeanManager = webinfClassesImplementation.getBeanManager();
        Set<Bean<?>> accessibleImplementations = webinfClassesBeanManager.getBeans(Animal.class);
        assertEquals(3, accessibleImplementations.size());

        for (Bean<?> bean : accessibleImplementations) {
            Animal animal = (Animal) webinfClassesBeanManager.getReference(bean, Animal.class,
                    webinfClassesBeanManager.createCreationalContext(bean));
            validateAccessibility(animal.getBeanManager());
        }
    }

    protected void validateAccessibility(BeanManager manager) {
        Set<Bean<?>> accessibleImplementations = manager.getBeans(Animal.class);
        assertEquals(3, accessibleImplementations.size());
    }
}
