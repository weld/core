/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.servlet.deployment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;
import org.jboss.weld.environment.deployment.discovery.DefaultBeanArchiveScanner;
import org.jboss.weld.environment.servlet.util.Servlets;
import org.jboss.weld.resources.ManagerObjectFactory;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * Web application bean archive scanner.
 *
 * @author Martin Kouba
 */
public class WebAppBeanArchiveScanner extends DefaultBeanArchiveScanner {

    private static final Logger log = Logger.getLogger(WebAppBeanArchiveScanner.class);

    static final String WEB_INF_BEANS_XML = "/WEB-INF/beans.xml";

    static final String WEB_INF_CLASSES_BEANS_XML = "/WEB-INF/classes/META-INF/beans.xml";

    static final String[] RESOURCES = {WEB_INF_BEANS_XML, WEB_INF_CLASSES_BEANS_XML};

    static final String WEB_INF_CLASSES = "/WEB-INF/classes";

    private final ServletContext servletContext;

    /**
     *
     * @param resourceLoader
     * @param bootstrap
     * @param servletContext
     */
    public WebAppBeanArchiveScanner(ResourceLoader resourceLoader, Bootstrap bootstrap, ServletContext servletContext) {
        super(resourceLoader, bootstrap);
        this.servletContext = servletContext;
    }

    @Override
    public Collection<BeanArchiveBuilder> scan(List<BeanArchiveHandler> beanArchiveHandlers) {

        // Scan libraries from WEB-INF/lib first
        Collection<BeanArchiveBuilder> beanArchiveBuilders = super.scan(beanArchiveHandlers);

        // WEB-INF/classes
        try {
            URL beansXmlUrl = null;
            for (String resource : RESOURCES) {
                URL resourceUrl = servletContext.getResource(resource);
                if(beansXmlUrl != null) {
                    log.warnv("Found both WEB-INF/beans.xml and WEB-INF/classes/META-INF/beans.xml. It's not portable to use both locations at the same time. Weld is going to use {0}.", beansXmlUrl);
                } else {
                    beansXmlUrl = resourceUrl;
                }
            }
            if (beansXmlUrl != null) {
                BeanArchiveBuilder builder;
                File webInfClasses = Servlets.getRealFile(servletContext, WEB_INF_CLASSES);
                if (webInfClasses != null) {
                    builder = handle(webInfClasses.getPath(), beanArchiveHandlers);
                } else {
                    // The WAR is not extracted to the file system - make use of ServletContext.getResourcePaths()
                    builder = handle(WEB_INF_CLASSES, Collections.<BeanArchiveHandler> singletonList(new ServletContextBeanArchiveHandler(servletContext)));
                }
                if (builder != null) {
                    beanArchiveBuilders.add(builder.setId(ManagerObjectFactory.FLAT_BEAN_DEPLOYMENT_ID).setBeansXmlUrl(beansXmlUrl)
                            .setBeansXml(bootstrap.parse(beansXmlUrl)));
                }
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Error loading resources from servlet context ", e);
        }
        return beanArchiveBuilders;
    }

}
