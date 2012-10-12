/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.se.discovery.url;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.environment.se.discovery.ImmutableBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Scan the classloader
 *
 * @author Thomas Heute
 * @author Gavin King
 * @author Norman Richards
 * @author Pete Muir
 * @author Peter Royle
 * @author Marko Luksa
 */
public class URLScanner {

    private static final Logger log = LoggerFactory.getLogger(URLScanner.class);
    private final String[] resources;
    private final ResourceLoader resourceLoader;
    private final Bootstrap bootstrap;

    public URLScanner(ResourceLoader resourceLoader, Bootstrap bootstrap, String... resources) {
        this.resources = resources;
        this.resourceLoader = resourceLoader;
        this.bootstrap = bootstrap;
    }

    public BeanDeploymentArchive scan() {
        FileSystemURLHandler handler = new FileSystemURLHandler();
        for (String resourceName : resources) {
            // grab all the URLs for this resource
            for (URL url : resourceLoader.getResources(resourceName)) {
                try {
                    handler.handle(getUrlPath(resourceName, url));
                } catch (URISyntaxException e) {
                    log.warn("could not read: " + resourceName, e);
                }
            }
        }
        return new ImmutableBeanDeploymentArchive("classpath", handler.getDiscoveredClasses(), bootstrap.parse(handler.getDiscoveredBeansXmlUrls(), true));
    }

    private String getUrlPath(String resourceName, URL url) throws URISyntaxException {
        String urlPath = url.toExternalForm();
        String urlType = getUrlType(urlPath);
        log.debug("URL Type: " + urlType);
        boolean isFile = "file".equals(urlType);
        boolean isJar = "jar".equals(urlType);
        // Extra built-in support for simple file-based resources
        if (isFile || isJar) {
            // switch to using toURI().getSchemeSpecificPart() instead of toExternalForm()
            urlPath = url.toURI().getSchemeSpecificPart();

            if (isJar && urlPath.lastIndexOf("!/") > 0) {   // according to JarURLConnection api doc, the separator is "!/"
                urlPath = urlPath.substring(0, urlPath.lastIndexOf("!/"));
                if (urlPath.startsWith("file:")) {
                    urlPath = urlPath.substring(5);
                }
            } else {
                // hack for /META-INF/beans.xml
                File dirOrArchive = new File(urlPath);
                if ((resourceName != null) && (resourceName.lastIndexOf('/') > 0)) {
                    dirOrArchive = dirOrArchive.getParentFile();
                }
                urlPath = dirOrArchive.getParent();
            }
        }

        return urlPath;
    }

    /**
     * determine resource type (eg: jar, file, bundle)
     */
    private String getUrlType(String urlPath) {
        String urlType = "file";
        int colonIndex = urlPath.indexOf(":");
        if (colonIndex != -1) {
            urlType = urlPath.substring(0, colonIndex);
        }
        return urlType;
    }

}
