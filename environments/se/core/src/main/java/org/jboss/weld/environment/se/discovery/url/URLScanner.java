/*
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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.reflection.Reflections;

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

    private static final String UNABLE_TO_CREATE_JANDEX_URL_HANDLER = "Unable to create the URLHandler instance when the jandex is enabled.";
    private static final String BAD_URI_SYNTAX_EXCEPTION_TEXT = "Could not read URI: ";
    private static final String JANDEX_ENABLED_FS_URL_HANDLER_CLASS_STRING = "org.jboss.weld.environment.se.discovery.url.JandexEnabledFileSystemURLHandler";
    private static final Logger log = Logger.getLogger(URLScanner.class);
    private static final String FILE = "file";
    // according to JarURLConnection api doc, the separator is "!/"
    private static final String SEPARATOR = "!/";

    private final String[] resources;
    private final ResourceLoader resourceLoader;
    private final Bootstrap bootstrap;
    private Collection<BeanArchiveBuilder> builders = new ArrayList<BeanArchiveBuilder>();

    public URLScanner(ResourceLoader resourceLoader, Bootstrap bootstrap, String... resources) {
        this.resources = resources;
        this.resourceLoader = resourceLoader;
        this.bootstrap = bootstrap;
    }

    public Collection<BeanArchiveBuilder> scan() {
        URLHandler handler = null;
        for (String resourceName : resources) {
            // grab all the URLs for this resource
            for (URL url : resourceLoader.getResources(resourceName)) {
                if (Reflections.isClassLoadable(Weld.JANDEX_INDEX_CLASS, resourceLoader)) {
                    Class<?> clazz = Reflections.loadClass(JANDEX_ENABLED_FS_URL_HANDLER_CLASS_STRING, resourceLoader);
                    try {
                        handler = (URLHandler) clazz.getConstructor(String.class, Bootstrap.class).newInstance(getUrlPath(resourceName, url).toString(),
                                bootstrap);
                    } catch (Exception ex) {
                        throw new IllegalStateException(UNABLE_TO_CREATE_JANDEX_URL_HANDLER, ex);
                    }
                } else {
                    try {
                        handler = new FileSystemURLHandler(getUrlPath(resourceName, url), bootstrap);
                    } catch (URISyntaxException e) {
                        throw new IllegalStateException(BAD_URI_SYNTAX_EXCEPTION_TEXT + resourceName, e);
                    }
                }
                try {
                    BeanArchiveBuilder builder = handler.handle(getUrlPath(resourceName, url));
                    builders.add(builder);
                } catch (URISyntaxException e) {
                    throw new IllegalStateException(BAD_URI_SYNTAX_EXCEPTION_TEXT + resourceName, e);
                }
            }
        }
        return builders;
    }

    private String getUrlPath(String resourceName, URL url) throws URISyntaxException {
        String urlPath = url.toExternalForm();
        String urlType = getUrlType(urlPath);
        log.debugv("URL Type: {0}", urlType);
        boolean isFile = FILE.equals(urlType);
        boolean isJar = "jar".equals(urlType);
        // Extra built-in support for simple file-based resources
        if (isFile || isJar) {
            // switch to using toURI().getSchemeSpecificPart() instead of toExternalForm()
            urlPath = url.toURI().getSchemeSpecificPart();

            if (isJar && urlPath.lastIndexOf(SEPARATOR) > 0) {
                urlPath = urlPath.substring(0, urlPath.lastIndexOf(SEPARATOR));
                final String fileUrlType = "file:";
                if (urlPath.startsWith(fileUrlType)) {
                    urlPath = urlPath.substring(fileUrlType.length());
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
     * Determine resource type (eg: jar, file, bundle)
     */
    private String getUrlType(String urlPath) {
        String urlType = FILE;
        int colonIndex = urlPath.indexOf(":");
        if (colonIndex != -1) {
            urlType = urlPath.substring(0, colonIndex);
        }
        return urlType;
    }

}
