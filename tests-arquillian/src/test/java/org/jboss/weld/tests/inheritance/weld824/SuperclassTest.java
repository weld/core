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
package org.jboss.weld.tests.inheritance.weld824;

import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class SuperclassTest {
    @Inject
    private Bar bar;

    /**
     * Webapp with beans.xml and no classes
     */
    @Deployment
    public static WebArchive createWebArchive() {
        WebArchive war = ShrinkWrap.create(WebArchive.class,
                Utils.getDeploymentNameAsHash(SuperclassTest.class, Utils.ARCHIVE_TYPE.WAR));
        war.addAsLibrary(createJavaArchive());
        war.addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
        return war;
    }

    /**
     * Java library with two classes (Foo, Bar) and no beans.xml - thus the classes are not beans implicitly.
     * However, the Bar class is registered through the SimpleExtension.
     */
    public static JavaArchive createJavaArchive() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "test.jar");
        jar.addClasses(SimpleExtension.class, Foo.class, Bar.class);
        jar.addAsServiceProvider(Extension.class, SimpleExtension.class);
        return jar;
    }

    @Test
    public void testSubclassInitialized() {
        assertTrue(bar.isSubclassInitialized());
        assertTrue(bar.isSuperclassInitialized());
    }
}
