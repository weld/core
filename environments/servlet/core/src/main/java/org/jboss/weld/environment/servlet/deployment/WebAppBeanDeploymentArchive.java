/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.servlet.deployment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.environment.servlet.util.Reflections;
import org.jboss.weld.environment.servlet.util.Servlets;

/**
 * The means by which Web Beans are discovered on the classpath. This will only discover simple web beans - there is no EJB/Servlet/JPA integration.
 *
 * @author Peter Royle
 * @author Pete Muir
 * @author Ales Justin
 */
public class WebAppBeanDeploymentArchive implements BeanDeploymentArchive {

    private static final Logger log = Logger.getLogger(WebAppBeanDeploymentArchive.class);

    public static final String META_INF_BEANS_XML = "META-INF/beans.xml";
    public static final String WEB_INF_BEANS_XML = "/WEB-INF/beans.xml";
    public static final String WEB_INF_CLASSES = "/WEB-INF/classes";
    private static final String SLASH = "/";
    private static final String DOT = ".";

    private final Set<String> classes;
    private final BeansXml beansXml;
    private final ServiceRegistry services;

    public WebAppBeanDeploymentArchive(ServletContext servletContext, Bootstrap bootstrap, URLScanner scanner) {
        this.services = new SimpleServiceRegistry();
        this.classes = new HashSet<String>();
        Set<URL> urls = new HashSet<URL>();

        if (scanner == null) {
            // Create the default scanner
            scanner = new URLScanner(Reflections.getClassLoader());
        }

        scanner.scanResources(new String[] { META_INF_BEANS_XML }, classes, urls);
        try {
            URL beans = servletContext.getResource(WEB_INF_BEANS_XML);
            if (beans != null) {
                urls.add(beans); // this is consistent with how the JBoss weld.deployer works
                File webInfClasses = Servlets.getRealFile(servletContext, WEB_INF_CLASSES);
                if (webInfClasses != null) {
                    File[] files = { webInfClasses };
                    scanner.scanDirectories(files, classes, urls);
                } else {
                    if (scanner.isURLHandlingSupported()) {
                        URL url = servletContext.getResource(WEB_INF_CLASSES);
                        if (url != null) {
                            scanner.scanURLs(new URL[] { url }, classes, urls);
                        }
                    } else {
                        // Make use of ServletContext.getResourcePaths()
                        handleResourcePath(WEB_INF_CLASSES, classes, servletContext);
                    }
                }
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Error loading resources from servlet context ", e);
        }
        this.beansXml = bootstrap.parse(urls, true);
    }

    public Collection<String> getBeanClasses() {
        return classes;
    }

    public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
        return Collections.emptySet();
    }

    public BeansXml getBeansXml() {
        return beansXml;
    }

    public Collection<EjbDescriptor<?>> getEjbs() {
        return Collections.emptySet();
    }

    public ServiceRegistry getServices() {
        return services;
    }

    public String getId() {
        // Use "flat" to allow us to continue to use ManagerObjectFactory
        return "flat";
    }

    static void handleResourcePath(String resourcePath, Set<String> classes, ServletContext servletContext) {

        log.debugv("Handling resource path: {0}", resourcePath);
        Set<String> subpaths = servletContext.getResourcePaths(resourcePath);

        if (subpaths != null && !subpaths.isEmpty()) {
            for (String subpath : subpaths) {
                if (subpath.endsWith(SLASH)) {
                    // Paths indicating subdirectory end with a '/'
                    handleResourcePath(subpath, classes, servletContext);
                } else if (subpath.endsWith(URLScanner.CLASS_FILENAME_EXTENSION)) {
                    // Class file
                    String className = toClassName(subpath);
                    classes.add(className);
                    log.debugv("Class discovered: {0}", className);
                }
            }
        }
    }

    private static String toClassName(String resourcePath) {
        // Remove WEB-INF/classes part, suffix and replace slashes with dots
        return resourcePath.substring(WEB_INF_CLASSES.length() + 1, resourcePath.lastIndexOf(URLScanner.CLASS_FILENAME_EXTENSION)).replace(SLASH, DOT);
    }

}
