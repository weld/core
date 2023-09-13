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
package org.jboss.weld.environment.se.test.discovery.handler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;
import java.util.Collections;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.deployment.AbstractWeldDeployment;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class AdditionalBeanArchiveHandlerTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class).addPackage(AdditionalBeanArchiveHandlerTest.class.getPackage()))
                .build();
    }

    @Test
    public void testAdditionalBeanArchiveHandlerUsed() {

        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            @Override
            public URLStreamHandler createURLStreamHandler(String protocol) {
                return new TestURLStreamHandler();
            }
        });

        try (WeldContainer container = new Weld().setResourceLoader(new TestResourceLoader()).initialize()) {
            // Bar is available, Foo is not (TestBeanArchiveHandler2 has higher priority)
            assertTrue(container.select(Bar.class).isResolvable());
            assertFalse(container.select(Foo.class).isResolvable());
        }
    }

    static class TestResourceLoader implements ResourceLoader {

        @Override
        public void cleanup() {
        }

        @Override
        public Class<?> classForName(String name) {
            try {
                return getClass().getClassLoader().loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new ResourceLoadingException(e);
            }
        }

        @Override
        public URL getResource(String name) {
            if (name.equals(AbstractWeldDeployment.BEANS_XML)) {
                try {
                    return new URL("uberjar://some/beans.xml");
                } catch (MalformedURLException e) {
                    throw new ResourceLoadingException(e);
                }
            }
            return null;
        }

        @Override
        public Collection<URL> getResources(String name) {
            if (name.equals(AbstractWeldDeployment.BEANS_XML)) {
                try {
                    return Collections.singleton(new URL("uberjar://some/beans.xml"));
                } catch (MalformedURLException e) {
                    throw new ResourceLoadingException(e);
                }
            }
            if (name.contains(BeanArchiveHandler.class.getName())) {
                try {
                    return Collections.singleton(new URL("uberjar://some/services"));
                } catch (MalformedURLException e) {
                    throw new ResourceLoadingException(e);
                }
            }
            return Collections.emptyList();
        }

    }

    static class TestURLStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new TestURLConnection(u);
        }

    }

    static class TestURLConnection extends URLConnection {

        protected TestURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (url.toString().contains("beans.xml")) {
                return new ByteArrayInputStream("".getBytes());
            } else if (url.toString().contains("services")) {
                return new ByteArrayInputStream(
                        (TestBeanArchiveHandler1.class.getName() + "\n" + TestBeanArchiveHandler2.class.getName()).getBytes());
            }
            return null;
        }

    }
}