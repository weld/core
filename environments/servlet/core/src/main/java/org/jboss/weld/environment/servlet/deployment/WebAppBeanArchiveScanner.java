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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletContext;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.discovery.DefaultBeanArchiveScanner;
import org.jboss.weld.environment.servlet.logging.WeldServletLogger;
import org.jboss.weld.environment.servlet.util.Servlets;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.collections.ImmutableList;

/**
 * Web application bean archive scanner.
 *
 * @author Martin Kouba
 */
public class WebAppBeanArchiveScanner extends DefaultBeanArchiveScanner {

    static final String WEB_INF = "/WEB-INF";

    static final String WEB_INF_CLASSES = WEB_INF + "/classes";

    static final String WEB_INF_BEANS_XML = WEB_INF + "/beans.xml";

    static final String WEB_INF_CLASSES_BEANS_XML = WEB_INF_CLASSES + "/META-INF/beans.xml";

    static final String WEB_INF_CLASSES_FILE_PATH = File.separatorChar + "WEB-INF" + File.separatorChar + "classes";

    static final String[] RESOURCES = { WEB_INF_BEANS_XML, WEB_INF_CLASSES_BEANS_XML };

    private final ServletContext servletContext;

    /**
     *
     * @param resourceLoader
     * @param bootstrap
     * @param servletContext
     */
    public WebAppBeanArchiveScanner(ResourceLoader resourceLoader, Bootstrap bootstrap, ServletContext servletContext,
            BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        super(resourceLoader, bootstrap, emptyBeansXmlDiscoveryMode);
        this.servletContext = servletContext;
    }

    @Override
    public List<ScanResult> scan() {

        // We use context path as a base for bean archive ids
        String contextPath = servletContext.getContextPath();
        List<ScanResult> results = new ArrayList<>(super.scan());

        File webInfClasses = null;
        ScanResult webInfBeansXML = null;

        try {
            // WEB-INF/classes
            URL beansXmlUrl = null;
            for (String resource : RESOURCES) {
                URL resourceUrl;
                resourceUrl = servletContext.getResource(resource);
                if (resourceUrl != null) {
                    if (beansXmlUrl != null) {
                        WeldServletLogger.LOG.foundBothConfiguration(beansXmlUrl);
                    } else {
                        beansXmlUrl = resourceUrl;
                    }
                }
            }
            if (beansXmlUrl != null) {
                BeansXml beansXml = parseBeansXml(beansXmlUrl);
                if (accept(beansXml)) {
                    webInfClasses = Servlets.getRealFile(servletContext, WEB_INF_CLASSES);
                    if (webInfClasses != null) {
                        webInfBeansXML = new ScanResult(beansXml, webInfClasses.getPath()).extractBeanArchiveId(contextPath,
                                WEB_INF);
                    } else {
                        // The WAR is not extracted to the file system - make use of ServletContext.getResourcePaths()
                        webInfBeansXML = new ScanResult(beansXml, WEB_INF_CLASSES);
                    }
                }
            }
        } catch (MalformedURLException e) {
            throw WeldServletLogger.LOG.errorLoadingResources(e);
        }

        // All previous results for WEB-INF/classes must be ignored
        for (Iterator<ScanResult> iterator = results.iterator(); iterator.hasNext();) {
            ScanResult result = iterator.next();
            String path = result.getBeanArchiveRef().toString();
            if (path.contains(WEB_INF_CLASSES_FILE_PATH) || path.contains(WEB_INF_CLASSES)
                    || new File(path).equals(webInfClasses)) {
                iterator.remove();
            } else {
                result.extractBeanArchiveId(contextPath, WEB_INF);
            }
        }

        if (webInfBeansXML != null) {
            results.add(webInfBeansXML);
        }

        return ImmutableList.copyOf(results);
    }
}
