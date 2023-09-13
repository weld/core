/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.annotatedType.annotation;

import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that {@link ProcessAnnotatedType} is not fired for an annotation registered using
 * {@link BeforeBeanDiscovery#addAnnotatedType(jakarta.enterprise.inject.spi.AnnotatedType)} or
 * {@link AfterTypeDiscovery#addAnnotatedType(jakarta.enterprise.inject.spi.AnnotatedType, String)}
 *
 * @author Jozef Hartinger
 *
 * @see CDI-320
 * @see WELD-1630
 */
@RunWith(Arquillian.class)
public class SyntheticAnnotationTest {

    @Inject
    private SyntheticAnnotationRegisteringExtension extension;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SyntheticAnnotationTest.class))
                .addPackage(SyntheticAnnotationTest.class.getPackage())
                .addAsServiceProvider(Extension.class, SyntheticAnnotationRegisteringExtension.class);
    }

    @Test
    public void testProcessAnnotatedTypeNotFiredForSyntheticAnnotation() {
        Assert.assertEquals(0, extension.getEventCount());
    }
}
