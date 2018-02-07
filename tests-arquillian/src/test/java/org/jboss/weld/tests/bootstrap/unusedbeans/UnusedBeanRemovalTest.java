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
package org.jboss.weld.tests.bootstrap.unusedbeans;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * See also WELD-2457.
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class UnusedBeanRemovalTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(BeanArchive.class,
                        Utils.getDeploymentNameAsHash(UnusedBeanRemovalTest.class))
                .addPackage(UnusedBeanRemovalTest.class.getPackage())
                .addAsServiceProvider(Extension.class, TestExtension.class)
                .addAsServiceProvider(Service.class, TestExternalConfiguration.class)
                .addAsResource(PropertiesBuilder.newBuilder()
                        .set(ConfigurationKey.UNUSED_BEANS_EXCLUDE_TYPE.get(),
                                "org\\.jboss\\.weld\\.tests\\.bootstrap\\.unusedbeans\\.Excluded.*")
                        .set(ConfigurationKey.UNUSED_BEANS_EXCLUDE_ANNOTATION.get(),
                                ".*UnusedExcludeAnnotation")
                        .build(), "weld.properties");
    }

    @Inject
    BeanManager beanManager;

    @Test
    public void testExtensionNotRemoved() {
        assertEquals(1, beanManager.getBeans(TestExtension.class).size());
    }

    @Test
    public void testUnusedRemoved() {
        assertEquals(0, beanManager.getBeans(Unused.class).size());
    }

    @Test
    public void testExcludedUnusedNotRemoved() {
        assertEquals(1, beanManager.getBeans(ExcludedUnused.class).size());
    }

    @Test
    public void testUnusedWithObserverNotRemoved() {
        assertEquals(1, beanManager.getBeans(UnusedWithObserver.class).size());
    }

    @Test
    public void testUnusedWithNameNotRemoved() {
        assertEquals(1, beanManager.getBeans(UnusedWithName.class).size());
    }

    @SuppressWarnings("serial")
    @Test
    public void testUnusedWithProducerNotRemoved() {
        assertEquals(1, beanManager.getBeans(UnusedWithProducer.class).size());
        assertEquals(1, beanManager.getBeans(new TypeLiteral<List<ExcludedUnused>>() {
        }.getType()).size());
    }

    @SuppressWarnings("serial")
    @Test
    public void testUnusedWithUnusedProducerRemoved() {
        assertEquals(0, beanManager.getBeans(UnusedWithUnusedProducer.class).size());
        assertEquals(0, beanManager.getBeans(new TypeLiteral<List<Unused>>() {
        }.getType()).size());
    }

    @Test
    public void testUnusedResolvableToInstanceNotRemoved() {
        assertEquals(1, beanManager.getBeans(UnusedResolvableToInstance.class).size());
    }

    @SuppressWarnings("serial")
    @Test
    public void testUnusedInterceptorNotRemoved() {
        assertEquals(1, beanManager.resolveInterceptors(InterceptionType.AROUND_INVOKE,
                new AnnotationLiteral<UnusedBinding>() {
                }).size());
    }

    @Test
    public void testUnusedWithExcludeAnnotationNotRemoved() {
        assertEquals(1, beanManager.getBeans(UnusedWithExcludeAnnotation.class).size());
    }

}
