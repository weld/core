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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipFile;

import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.discovery.AbstractWeldSEDeployment;
import org.jboss.weld.environment.se.logging.WeldSELogger;
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
 * @author Stefan Großmann
 */
public class URLScanner {

    private static final String JANDEX_ENABLED_FS_URL_HANDLER_CLASS_STRING = "org.jboss.weld.environment.se.discovery.url.JandexEnabledFileSystemURLHandler";
    private static final String JANDEX_INDEX_ENABLED_HANDLER_CLASS_STRING = "org.jboss.weld.environment.se.discovery.url.JandexIndexURLHandler";

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
        for (String resourceName : resources) {
            // grab all the URLs for this resource
            for (URL url : resourceLoader.getResources(resourceName)) {
                log.debugv("Scanning bean archive for {0}.", url);
                String urlPath;
                BeansXml beansXml = null;
                try {
                    urlPath = getUrlPath(resourceName, url);
                    beansXml = getBeansXML(url);
                    if (!mustScan(beansXml)) {
                        continue;
                    }
                } catch (URISyntaxException e) {
                    WeldSELogger.LOG.couldNotReadResource(resourceName, e);
                    continue;
                }

                final URLHandler handler = constructHandler(urlPath, beansXml);
                builders.add(handler.handle(urlPath).setId(getId(urlPath)));
            }
        }
        return builders;
    }

    private boolean mustScan(final BeansXml beansXml) {
        boolean mustScan = true;
        if (beansXml != null) {
            final BeanDiscoveryMode discoveryMode = beansXml.getBeanDiscoveryMode();
            if (discoveryMode == BeanDiscoveryMode.NONE) {
                mustScan = false;
            }
        }
        return mustScan;
    }

    private URLHandler constructHandler(String urlPath, final BeansXml beansXml) {
        final boolean jandexInClassPath = Reflections.isClassLoadable(Weld.JANDEX_INDEX_CLASS_NAME, resourceLoader);
        URLHandler handler;
        if (jandexInClassPath) {
            handler = SEReflections.newInstance(resourceLoader, JANDEX_INDEX_ENABLED_HANDLER_CLASS_STRING, beansXml);
            if (!handler.canHandle(urlPath)) {
                log.debugv("Not able to load JANDEX index for {}. Fallback to JandexEnabledFileSystemURLHandler.", beansXml);
                handler = SEReflections.newInstance(resourceLoader, JANDEX_ENABLED_FS_URL_HANDLER_CLASS_STRING, bootstrap, beansXml);
            }
        } else {
            handler = new FileSystemURLHandler(bootstrap, beansXml);
        }
        return handler;
    }

    private BeansXml getBeansXML(URL url) throws URISyntaxException {
        final String resourceUrlPath = url.toURI().getSchemeSpecificPart();
        BeansXml beansXml = null;
        // hack for /META-INF/beans.xml
        if (resourceUrlPath.endsWith(AbstractWeldSEDeployment.BEANS_XML)) {
            beansXml = bootstrap.parse(url);
        }

        return beansXml;
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

        if (urlPath.startsWith("http:") || urlPath.startsWith("https:")) {
            urlPath = convertWebstartToLocalPath(urlPath);
        }

        return urlPath;
    }

    /**
     * WebStart support: get path to local cached copy of remote JAR file
     */
    private String convertWebstartToLocalPath(String urlPath) {
        // Class loader should be an instance of com.sun.jnlp.JNLPClassLoader
        ClassLoader jnlpClassLoader = WeldSEResourceLoader.getClassLoader();
        try {
            // Try to call com.sun.jnlp.JNLPClassLoader#getJarFile(URL) from JDK 6
            Method m = jnlpClassLoader.getClass().getMethod("getJarFile", URL.class);
            // returns a reference to the local cached copy of the JAR
            ZipFile jarFile = (ZipFile) m.invoke(jnlpClassLoader, new URL(urlPath));
            urlPath = jarFile.getName();
        } catch (MalformedURLException mue) {
            WeldSELogger.LOG.couldNotReadEntries(urlPath, mue);
        } catch (NoSuchMethodException nsme) {
            WeldSELogger.LOG.unexpectedClassLoader(nsme);
        } catch (IllegalArgumentException iarge) {
            WeldSELogger.LOG.unexpectedClassLoader(iarge);
        } catch (InvocationTargetException ite) {
            WeldSELogger.LOG.jnlpClassLoaderInternalException(ite);
        } catch (Exception iacce) {
            WeldSELogger.LOG.jnlpClassLoaderInvocationException(iacce);
        }
        return urlPath;
    }

    /**
     * Create an ID that will be used for the bean archive calculated from the url path.
     */
    private String getId(String urlPath) {
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
