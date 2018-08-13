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
 * @see WELD-2523
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class AdditionalBeanDefiningAnnotationsTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder().add(ShrinkWrap.create(BeanArchive.class).addPackage(AdditionalBeanDefiningAnnotationsTest.class.getPackage()))
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

}
