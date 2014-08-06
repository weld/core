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
package org.jboss.weld.environment.deployment.discovery;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.AbstractWeldDeployment;
import org.jboss.weld.environment.deployment.WeldResourceLoader;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * Scans the classpath and tries to process all "META-INF/beans.xml" resources.
 *
 * @author Martin Kouba
 */
public class DefaultBeanArchiveScanner implements BeanArchiveScanner {

    private static final Logger log = Logger.getLogger(DefaultBeanArchiveScanner.class);

    static final String PROCOTOL_FILE = "file";

    static final String PROCOTOL_JAR = "jar";

    static final String PROCOTOL_HTTP = "http";

    static final String PROCOTOL_HTTPS = "https";

    private static final String PROTOCOL_FILE_PART = PROCOTOL_FILE + ":";

    // according to JarURLConnection api doc, the separator is "!/"
    private static final String JAR_URL_SEPARATOR = "!/";

    protected final ResourceLoader resourceLoader;

    protected final Bootstrap bootstrap;

    /**
     *
     * @param resourceLoader
     * @param bootstrap
     */
    public DefaultBeanArchiveScanner(ResourceLoader resourceLoader, Bootstrap bootstrap) {
        this.resourceLoader = resourceLoader;
        this.bootstrap = bootstrap;
    }

    @Override
    public Collection<BeanArchiveBuilder> scan(List<BeanArchiveHandler> beanArchiveHandlers) {

        Collection<BeanArchiveBuilder> beanArchives = new ArrayList<BeanArchiveBuilder>();
        Map<URL, BeansXml> beansXmlMap = new HashMap<URL, BeansXml>();
        // META-INF/beans.xml
        String[] resources = AbstractWeldDeployment.RESOURCES;

        // Find all beans.xml files
        for (String resourceName : resources) {
            for (URL beansXmlUrl : resourceLoader.getResources(resourceName)) {
                beansXmlMap.put(beansXmlUrl, bootstrap.parse(beansXmlUrl));
            }
        }

        for (Entry<URL, BeansXml> entry : beansXmlMap.entrySet()) {
            if (BeanDiscoveryMode.NONE.equals(entry.getValue().getBeanDiscoveryMode())) {
                // Do not scan bean archives with mode of none
                continue;
            }
            try {
                String ref = getBeanArchiveReference(entry.getKey());
                BeanArchiveBuilder builder = handle(ref, beanArchiveHandlers);
                if (builder != null) {
                    builder.setId(ref);
                    builder.setBeansXmlUrl(entry.getKey());
                    builder.setBeansXml(entry.getValue());
                    beanArchives.add(builder);
                }
            } catch (URISyntaxException e) {
                CommonLogger.LOG.couldNotReadResource(entry.getKey(), e);
                continue;
            }
        }
        return beanArchives;
    }

    protected BeanArchiveBuilder handle(String reference, List<BeanArchiveHandler> handlers) {
        BeanArchiveBuilder builder = null;
        for (BeanArchiveHandler handler : handlers) {
            builder = handler.handle(reference);
            if (builder != null) {
                break;
            }
        }
        if (builder == null) {
            log.warnv("The bean archive reference {0} cannot be handled by any BeanArchiveHandler: {1}", reference, handlers);
        }
        return builder;
    }

    /**
     * @param url
     * @return
     * @throws URISyntaxException
     */
    protected String getBeanArchiveReference(URL url) throws URISyntaxException {

        String ref = null;

        if(PROCOTOL_FILE.equals(url.getProtocol())) {
            // Adapt file URL, e.g. "file:///home/weld/META-INF/beans.xml" becomes "/home/weld"
            ref = new File(url.toURI().getSchemeSpecificPart()).getParentFile().getParent();

        } else if(PROCOTOL_JAR.equals(url.getProtocol())) {
            // Adapt JAR file URL, e.g. "jar:file:/home/duke/duke.jar!/META-INF/beans.xml" becomes "/home/duke/duke.jar"

            // The decoded part without protocol part, i.e. without "jar:"
            ref = url.toURI().getSchemeSpecificPart();

            if(ref.lastIndexOf(JAR_URL_SEPARATOR) > 0) {
                ref = ref.substring(0, ref.lastIndexOf(JAR_URL_SEPARATOR));
                if (ref.startsWith(PROTOCOL_FILE_PART)) {
                    ref = ref.substring(PROTOCOL_FILE_PART.length());
                }
            } else {
                log.warnv("Unable to adapt JAR file URL: {0}, using its external form instead", url);
                ref = url.toExternalForm();
            }
        } else {

            // WebStart support: get path to local cached copy of remote JAR file
            if (PROCOTOL_HTTP.equals(url.getProtocol()) || PROCOTOL_HTTPS.equals(url.getProtocol())) {
                // Class loader should be an instance of com.sun.jnlp.JNLPClassLoader
                ClassLoader jnlpClassLoader = WeldResourceLoader.getClassLoader();
                try {
                    // Try to call com.sun.jnlp.JNLPClassLoader#getJarFile(URL) from JDK 6
                    Method m = jnlpClassLoader.getClass().getMethod("getJarFile", URL.class);
                    // returns a reference to the local cached copy of the JAR
                    ZipFile jarFile = (ZipFile) m.invoke(jnlpClassLoader, url);
                    ref = jarFile.getName();
                } catch (NoSuchMethodException nsme) {
                    CommonLogger.LOG.unexpectedClassLoader(nsme);
                } catch (IllegalArgumentException iarge) {
                    CommonLogger.LOG.unexpectedClassLoader(iarge);
                } catch (InvocationTargetException ite) {
                    CommonLogger.LOG.jnlpClassLoaderInternalException(ite);
                } catch (Exception iacce) {
                    CommonLogger.LOG.jnlpClassLoaderInvocationException(iacce);
                }
            }

            if (ref == null) {
                ref = url.toExternalForm();
            }
        }
        log.debugv("Resolved bean archive reference: {0} for URL: {1}", ref, url);
        return ref;
    }



}
