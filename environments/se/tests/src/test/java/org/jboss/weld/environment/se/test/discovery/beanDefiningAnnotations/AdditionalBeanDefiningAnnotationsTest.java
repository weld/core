/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.discovery.beanDefiningAnnotations;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Adds new bean defining annotations in Weld SE, then leaves discovery on and asserts that beans were found.
 *
 * Also tests that you can add new BDA via purely CDI SE container properties and via system properties.
 *
 * @see WELD-2523
 * @see WELD-2639
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class AdditionalBeanDefiningAnnotationsTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class).addPackage(AdditionalBeanDefiningAnnotationsTest.class.getPackage()))
                .build();
    }

    @Test
    public void testNewBeanDefiningAnnotationWorks() {
        Weld weld = new Weld()
                .disableDiscovery()
                .setBeanDiscoveryMode(BeanDiscoveryMode.ANNOTATED)
                .addPackages(Bar.class.getPackage())
                .addBeanDefiningAnnotations(NewBeanDefiningAnnotation.class);

        try (WeldContainer container = weld.initialize()) {
            Assert.assertTrue(container.isRunning());
            Assert.assertTrue(container.select(Foo.class).isResolvable());
            Assert.assertTrue(container.select(Bar.class).isResolvable());
        }
    }

    @Test
    public void testDeclarationViaPropertiesWithWrongValue() {
        Weld weld = new Weld()
                .disableDiscovery()
                .setBeanDiscoveryMode(BeanDiscoveryMode.ANNOTATED)
                .addPackages(Bar.class.getPackage())
                .addProperty(Weld.ADDITIONAL_BEAN_DEFINING_ANNOTATIONS_PROPERTY, "foo");

        try (WeldContainer container = weld.initialize()) {
            // should throw up while booting
            Assert.fail();
        } catch (IllegalArgumentException expected) {
            // OK
        }
    }

    @Test
    public void testDeclarationViaPropertiesWithCorruptedSet() {
        // we log warning on each wrong item in the list, so this should pass and just skip wrong value
        Set<Object> corruptedSet = new HashSet<>();
        corruptedSet.add(NewBeanDefiningAnnotation.class);
        corruptedSet.add(1l);

        Weld weld = new Weld()
                .disableDiscovery()
                .setBeanDiscoveryMode(BeanDiscoveryMode.ANNOTATED)
                .addPackages(Bar.class.getPackage())
                .addProperty(Weld.ADDITIONAL_BEAN_DEFINING_ANNOTATIONS_PROPERTY, corruptedSet);

        try (WeldContainer container = weld.initialize()) {
            Assert.assertTrue(container.isRunning());
            Assert.assertTrue(container.select(Foo.class).isResolvable());
            Assert.assertTrue(container.select(Bar.class).isResolvable());
        }
    }

    @Test
    public void testCorrectDeclarationViaProperties() {
        // we log warning on each wrong item in the list, so this should pass and just skip wrong value
        Set<Class<? extends Annotation>> correctSet = new HashSet<>();
        correctSet.add(NewBeanDefiningAnnotation.class);

        Weld weld = new Weld()
                .disableDiscovery()
                .setBeanDiscoveryMode(BeanDiscoveryMode.ANNOTATED)
                .addPackages(Bar.class.getPackage())
                .addProperty(Weld.ADDITIONAL_BEAN_DEFINING_ANNOTATIONS_PROPERTY, correctSet);

        try (WeldContainer container = weld.initialize()) {
            Assert.assertTrue(container.isRunning());
            Assert.assertTrue(container.select(Foo.class).isResolvable());
            Assert.assertTrue(container.select(Bar.class).isResolvable());
        }
    }

    @Test
    public void testCorrectDeclarationViaSystemProperties() {
        setupSystemProperty(true, false, false);
        Weld weld = new Weld()
                .disableDiscovery()
                .setBeanDiscoveryMode(BeanDiscoveryMode.ANNOTATED)
                .addPackages(Bar.class.getPackage());

        try (WeldContainer container = weld.initialize()) {
            Assert.assertTrue(container.isRunning());
            Assert.assertTrue(container.select(Foo.class).isResolvable());
            Assert.assertTrue(container.select(Bar.class).isResolvable());
        } finally {
            clearSystemProperty();
        }
    }

    @Test
    public void testDeclarationViaSystemPropertiesWithCorruptedList() {
        setupSystemProperty(true, true, false);
        Weld weld = new Weld()
                .disableDiscovery()
                .setBeanDiscoveryMode(BeanDiscoveryMode.ANNOTATED)
                .addPackages(Bar.class.getPackage());

        try (WeldContainer container = weld.initialize()) {
            Assert.assertTrue(container.isRunning());
            Assert.assertTrue(container.select(Foo.class).isResolvable());
            Assert.assertTrue(container.select(Bar.class).isResolvable());
        } finally {
            clearSystemProperty();
        }
    }

    @Test
    public void testDeclarationViaSystemPropertiesWithWrongValue() {
        setupSystemProperty(true, false, true);
        Weld weld = new Weld()
                .disableDiscovery()
                .setBeanDiscoveryMode(BeanDiscoveryMode.ANNOTATED)
                .addPackages(Bar.class.getPackage());

        try (WeldContainer container = weld.initialize()) {
            Assert.fail();
        } catch (IllegalArgumentException expected) {
            // OK, this should blow up
            System.err.println(expected);
        } finally {
            clearSystemProperty();
        }
    }

    private void setupSystemProperty(boolean correctClass, boolean wrongType, boolean nonExistentClass) {
        Properties props = System.getProperties();
        StringBuilder builder = new StringBuilder();
        if (correctClass) {
            builder.append(NewBeanDefiningAnnotation.class.getName()).append(",");
        }
        if (wrongType) {
            builder.append(Integer.class.getName()).append(",");
        }
        if (nonExistentClass) {
            builder.append("foo.bar.Nope");
        }
        props.setProperty(Weld.ADDITIONAL_BEAN_DEFINING_ANNOTATIONS_PROPERTY, builder.toString());
    }

    private void clearSystemProperty() {
        Properties props = System.getProperties();
        props.remove(Weld.ADDITIONAL_BEAN_DEFINING_ANNOTATIONS_PROPERTY);
    }
}
