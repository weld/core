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
package org.jboss.weld.environment.se.test.arquillian;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class WeldSEDeployableContainer implements DeployableContainer<WeldSEContainerConfiguration> {

    private static final String PROTOCOL_NAME = "Local";

    private Weld weld;

    @Inject
    @DeploymentScoped
    private InstanceProducer<BeanManager> beanManagerProducer;

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        final URL[] urls = writeFiles(archive);
        final ClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader()) {
            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                Enumeration<URL> resources = super.getResources(name);
                Set<URL> urls = new HashSet<URL>();
                while (resources.hasMoreElements()) {
                    final URL url = resources.nextElement();
                    // ignore any resources on classpath outside of the deployment
                    if (url.getProtocol().equalsIgnoreCase("jar")) {
                        urls.add(url);
                    }
                }
                return Collections.enumeration(urls);
            }
        };
        this.weld = new Weld().setClassLoader(classLoader);
        final WeldContainer container = this.weld.initialize();
        beanManagerProducer.set(container.getBeanManager());
        return new ProtocolMetaData();
    }

    private URL[] writeFiles(Archive<?> archive) {
        if (archive instanceof WeldSEClassPathImpl) {
            WeldSEClassPathImpl classPath = (WeldSEClassPathImpl) archive;
            URL[] urls = new URL[classPath.getArchives().size()];
            int i = 0;
            for (JavaArchive jar : classPath.getArchives()) {
                urls[i++] = writeFile(jar);
            }
            return urls;
        } else {
            return new URL[] { writeFile(archive) };
        }
    }

    private URL writeFile(Archive<?> archive) {
        File file;
        try {
            file = File.createTempFile(archive.getId(), ".jar");
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        file.deleteOnExit();
        archive.as(ZipExporter.class).exportTo(file, true);
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        if (weld != null) {
            weld.shutdown();
        }
    }

    @Override
    public Class<WeldSEContainerConfiguration> getConfigurationClass() {
        return WeldSEContainerConfiguration.class;
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription(PROTOCOL_NAME);
    }

    @Override
    public void setup(WeldSEContainerConfiguration arg0) {
    }

    @Override
    public void start() throws LifecycleException {
        // noop
    }

    @Override
    public void stop() throws LifecycleException {
        // noop
    }

    @Override
    public void undeploy(Descriptor arg0) throws DeploymentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deploy(Descriptor arg0) throws DeploymentException {
        throw new UnsupportedOperationException();
    }
}
