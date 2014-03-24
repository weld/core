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
import org.jboss.weld.environment.se.util.SEReflections;
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

    private static final String JANDEX_ENABLED_FS_URL_HANDLER_CLASS_STRING = "org.jboss.weld.environment.se.discovery.url.JandexEnabledFileSystemURLHandler";
    private static final Logger log = Logger.getLogger(URLScanner.class);
    private static final String FILE = "file";
    // according to JarURLConnection api doc, the separator is "!/"
    private static final String SEPARATOR = "!/";

    private final String[] resources;
    private final ResourceLoader resourceLoader;
    private final Bootstrap bootstrap;
    private final Collection<BeanArchiveBuilder> builders = new ArrayList<BeanArchiveBuilder>();

    public URLScanner(ResourceLoader resourceLoader, Bootstrap bootstrap, String... resources) {
        this.resources = resources;
        this.resourceLoader = resourceLoader;
        this.bootstrap = bootstrap;
    }

    /**
     * Scan all the resources and create {@link BeanArchiveBuilder} for each
     *
     * @return Collection<BeanArchiveBuilder> collection of the {@link BeanArchiveBuilder}-s that were created.
     */
    public Collection<BeanArchiveBuilder> scan() {
        URLHandler handler = null;
        for (String resourceName : resources) {
            // grab all the URLs for this resource
            for (URL url : resourceLoader.getResources(resourceName)) {
                String urlPath;
                try {
                    urlPath = getUrlPath(resourceName, url);
                } catch (URISyntaxException e) {
                    log.warn("Could not read: " + resourceName, e);
                    continue;
                }
                final String bdaId = getId(urlPath);
                if (Reflections.isClassLoadable(Weld.JANDEX_INDEX_CLASS_NAME, resourceLoader)) {
                    handler = SEReflections.newInstance(resourceLoader, JANDEX_ENABLED_FS_URL_HANDLER_CLASS_STRING, bootstrap);
                } else {
                    handler = new FileSystemURLHandler(bootstrap);
                }
                BeanArchiveBuilder builder = handler.handle(urlPath);
                builder.setId(bdaId);
                builders.add(builder);
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
     * Create an ID that will be used for the bean archive calculated from the url path.
     */
    private String getId(String urlPath) {
        final int index = urlPath.lastIndexOf(File.separatorChar);
        if (index != -1 && index + 1 < urlPath.length()) {
            return urlPath.substring(index + 1);
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
