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
package org.jboss.weld.environment.se.test.builder.scanning;

import static org.jboss.shrinkwrap.api.ShrinkWrap.create;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.weld.environment.deployment.AbstractWeldDeployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Simulates a scenario where we have a framework that creates its own classloader e.g. for a downloaded maven artifact
 * and boots it as an embedded application. The framework could spawn many independent CDI applications this way.
 * https://issues.jboss.org/browse/WELD-1956
 *
 * @author Jozef Hartinger
 *
 */
public class ExplicitClassLoaderScanningTest {

    @Test
    public void testScanningExplicitClassLoader() throws IOException {
        // this is the application
        final Archive<?> archive = create(BeanArchive.class).addClass(EmbeddedApplication.class);
        final File jar = File.createTempFile("weld-se-test", ".jar");
        jar.deleteOnExit();
        archive.as(ZipExporter.class).exportTo(jar, true);

        /*
         * Special classloader that hides BDAs in parent classloaders. This would not be needed normally. We need this here
         * because
         * , since this testsuite defines a top-level beans.xml file, each file in this testsuite is already part of this single
         * giant BDA.
         * Since we are adding the EmbeddedApplication class to the special BDA we test, we do not want the class to be found
         * twice. We cannot just leave
         * out the parent classloader as we need CDI classes to be loadable from the application.
         */
        ClassLoader classLoader = new URLClassLoader(new URL[] { jar.toURI().toURL() }) {
            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                if (AbstractWeldDeployment.BEANS_XML.equals(name)) {
                    return findResources(name);
                }
                return super.getResources(name);
            }
        };

        try (WeldContainer weld = new Weld().setClassLoader(classLoader).initialize()) {
            AtomicInteger payload = new AtomicInteger();
            weld.event().fire(payload);
            Assert.assertEquals(10, payload.intValue());
        }
    }
}
