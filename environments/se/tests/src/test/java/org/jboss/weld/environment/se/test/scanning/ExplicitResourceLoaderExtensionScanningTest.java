/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.scanning;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ExplicitResourceLoaderExtensionScanningTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        final JavaArchive bda1 = ShrinkWrap.create(JavaArchive.class)
                .addClasses(ExplicitResourceLoaderExtensionScanningTest.class, MyExtension.class, Alpha.class, Bravo.class,
                        AlphaExtension.class,
                        BravoExtension.class, DummyBean.class)
                .addAsManifestResource(new BeansXml(), "beans.xml")
                .addAsServiceProvider(Extension.class, AlphaExtension.class, BravoExtension.class)
                .addAsServiceProvider(MyExtension.class, AlphaExtension.class);
        return ClassPath.builder().add(bda1).build();
    }

    @Test
    public void testScanning() {

        ClassLoader classLoader = new URLClassLoader(new URL[] {}, Alpha.class.getClassLoader()) {
            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                if ("META-INF/services/jakarta.enterprise.inject.spi.Extension".equals(name)) {
                    // Load only AlphaExtension
                    return super.getResources("META-INF/services/" + MyExtension.class.getName());
                }
                return super.getResources(name);
            }
        };

        try (WeldContainer container = new Weld().setResourceLoader(new ClassLoaderResourceLoader(classLoader)).initialize()) {
            container.select(Alpha.class).get().ping();
            assertTrue(container.select(Bravo.class).isUnsatisfied());
        }
    }

}
