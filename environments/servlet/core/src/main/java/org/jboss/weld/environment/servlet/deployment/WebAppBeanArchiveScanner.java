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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.discovery.DefaultBeanArchiveScanner;
import org.jboss.weld.environment.servlet.logging.WeldServletLogger;
import org.jboss.weld.environment.servlet.util.Servlets;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * Web application bean archive scanner.
 *
 * @author Martin Kouba
 */
public class WebAppBeanArchiveScanner extends DefaultBeanArchiveScanner {

    static final String WEB_INF_BEANS_XML = "/WEB-INF/beans.xml";

    static final String WEB_INF_CLASSES_BEANS_XML = "/WEB-INF/classes/META-INF/beans.xml";

    static final String[] RESOURCES = { WEB_INF_BEANS_XML, WEB_INF_CLASSES_BEANS_XML };

    static final String WEB_INF_CLASSES = "/WEB-INF/classes";

    static final String WEB_INF_CLASSES_FILE_PATH = File.separatorChar + "WEB-INF" + File.separatorChar + "classes";

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
    public Map<URL, ScanResult> scan() {

        Map<URL, ScanResult> beansXmlMap = super.scan();

        // All previous results for WEB-INF/classes must be ignored
        for (Iterator<Entry<URL, ScanResult>> iterator = beansXmlMap.entrySet().iterator(); iterator.hasNext();) {
            String path = iterator.next().getKey().toString();
            if (path.contains(WEB_INF_CLASSES_FILE_PATH) || path.contains(WEB_INF_CLASSES)) {
                iterator.remove();
            }
        }

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
                BeansXml beansXml = bootstrap.parse(beansXmlUrl);
                if (accept(beansXml)) {
                    File webInfClasses = Servlets.getRealFile(servletContext, WEB_INF_CLASSES);
                    if (webInfClasses != null) {
                        beansXmlMap.put(beansXmlUrl, new ScanResult(beansXml, webInfClasses.getPath()));
                    } else {
                        // The WAR is not extracted to the file system - make use of ServletContext.getResourcePaths()
                        beansXmlMap.put(beansXmlUrl, new ScanResult(beansXml, WEB_INF_CLASSES));
                    }
                }
            }
        } catch (MalformedURLException e) {
            throw WeldServletLogger.LOG.errorLoadingResources(e);
        }
        return beansXmlMap;
    }
}
