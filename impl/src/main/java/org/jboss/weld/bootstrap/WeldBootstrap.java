/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap;

import java.net.URL;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.config.SystemPropertiesConfiguration;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.ServiceLoader;
import org.jboss.weld.xml.BeansXmlParser;
import org.jboss.weld.xml.BeansXmlStreamParser;
import org.jboss.weld.xml.BeansXmlValidator;

/**
 * Common bootstrapping functionality that is run at application startup and
 * detects and register beans
 *
 * @author Pete Muir
 * @author Ales Justin
 * @author Marko Luksa
 */
public class WeldBootstrap implements CDI11Bootstrap {

    private WeldStartup weldStartup;
    private WeldRuntime weldRuntime;

    private final BeansXmlValidator beansXmlValidator;

    public WeldBootstrap() {
        weldStartup = new WeldStartup();
        beansXmlValidator = SystemPropertiesConfiguration.INSTANCE.isXmlValidationDisabled() ? null : new BeansXmlValidator();
    }

    @Override
    public synchronized TypeDiscoveryConfiguration startExtensions(Iterable<Metadata<Extension>> extensions) {
        return weldStartup.startExtensions(extensions);
    }

    @Override
    public synchronized Bootstrap startContainer(Environment environment, Deployment deployment) {
        return startContainer(RegistrySingletonProvider.STATIC_INSTANCE, environment, deployment);
    }

    @Override
    public synchronized Bootstrap startContainer(String contextId, Environment environment, Deployment deployment) {
        weldRuntime = weldStartup.startContainer(contextId, environment, deployment);
        return this;
    }

    @Override
    public synchronized Bootstrap startInitialization() {
        checkInitializationNotAlreadyEnded();
        weldStartup.startInitialization();
        return this;
    }

    @Override
    public synchronized Bootstrap deployBeans() {
        checkInitializationNotAlreadyEnded();
        weldStartup.deployBeans();
        return this;
    }

    @Override
    public synchronized Bootstrap validateBeans() {
        checkInitializationNotAlreadyEnded();
        weldStartup.validateBeans();
        return this;
    }

    @Override
    public synchronized Bootstrap endInitialization() {
        if (weldStartup != null) {
            weldStartup.endInitialization();
            weldStartup = null;
        }
        return this;
    }


    @Override
    public synchronized BeanManagerImpl getManager(BeanDeploymentArchive beanDeploymentArchive) {
        return weldRuntime == null ? null : weldRuntime.getManager(beanDeploymentArchive);
    }


    @Override
    public synchronized void shutdown() {
        if (weldRuntime != null) {
            weldRuntime.shutdown();
            weldRuntime = null;
        }
    }

    @Override
    public BeansXml parse(Iterable<URL> urls) {
        return parse(urls, false);
    }

    @Override
    public BeansXml parse(Iterable<URL> urls, boolean removeDuplicates) {
        return parse(urls, removeDuplicates, BeanDiscoveryMode.ANNOTATED);
    }

    @Override
    public BeansXml parse(URL url) {
        return parse(url, BeanDiscoveryMode.ANNOTATED);
    }

    @Override
    public BeansXml parse(URL url, BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        if (beansXmlValidator != null) {
            beansXmlValidator.validate(url);
        }
        return new BeansXmlStreamParser(url, emptyBeansXmlDiscoveryMode).parse();    }

    @Override
    public BeansXml parse(Iterable<URL> urls, BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        return parse(urls, false, emptyBeansXmlDiscoveryMode);
    }

    @Override
    public BeansXml parse(Iterable<URL> urls, boolean removeDuplicates, BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        return BeansXmlParser.merge(urls, url -> parse(url, emptyBeansXmlDiscoveryMode), removeDuplicates);
    }

    @Override
    public Iterable<Metadata<Extension>> loadExtensions(ClassLoader classLoader) {
        return ServiceLoader.load(Extension.class, classLoader);
    }

    private void checkInitializationNotAlreadyEnded() {
        if (weldStartup == null) {
            throw BootstrapLogger.LOG.callingBootstrapMethodAfterContainerHasBeenInitialized();
        }
    }
}