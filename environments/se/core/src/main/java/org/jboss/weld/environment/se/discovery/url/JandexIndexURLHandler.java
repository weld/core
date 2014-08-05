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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.UnsupportedVersion;
import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.se.discovery.AbstractWeldSEDeployment;
import org.jboss.weld.environment.se.logging.WeldSELogger;

/**
 * This class uses a Jandex-Index ("META-INF/jandex.idx") to scan the the archive. If no index is available the {@link JandexIndexURLHandler#handle(String)}
 * method will return null. To prevent this, use {@link JandexIndexURLHandler#canHandle(String)} to check if an index is available and supported.
 *
 * @author Stefan Großmann
 */
public class JandexIndexURLHandler implements URLHandler {
    private static final Logger log = Logger.getLogger(JandexIndexURLHandler.class);

    private static final String JAR_URL_PREFIX = "jar:";
    private static final String FILE_URL_PREFIX = "file:";
    private static final String SEPARATOR = "!/";

    private final List<String> discoveredClasses = new ArrayList<String>();
    private final BeansXml beansXml;

    private Index indexCache = null;
    private String indexCacheUrlPath = null;

    public JandexIndexURLHandler(BeansXml beansXml) {
        this.beansXml = beansXml;
    }

    @Override
    public boolean canHandle(String urlPath) {
        return getIndex(urlPath) != null;
    }

    @Override
    public BeanArchiveBuilder handle(String urlPath) {
        Index index = getIndex(urlPath);
        handleArchiveByIndex(index);
        return createBeanArchiveBuilder(index);
    }

    private Index getIndex(final String urlPath) {
        if (urlPath == null) {
            throw new IllegalArgumentException("");
        }

        if (indexCacheUrlPath == null || !indexCacheUrlPath.equals(urlPath)) {
            Index newIndex = loadJandexIndex(urlPath);
            indexCache = newIndex;
            indexCacheUrlPath = urlPath;

        }

        return indexCache;
    }

    private Index loadJandexIndex(final String urlPath) {
        URL indexURL = null;
        Index index = null;

        final String indexUrlString = getJandexIndexURLString(urlPath);
        try {
            indexURL = new URL(indexUrlString);
        } catch (MalformedURLException e) {
            return null;
        }

        InputStream indexFileStream = null;
        try {
            indexFileStream = indexURL.openStream();
            final IndexReader indexFileReader = new IndexReader(indexFileStream);
            index = indexFileReader.read();
        } catch (IllegalArgumentException e) {
            log.debugv("Jandex index at {} is not valid", indexUrlString);
        } catch (UnsupportedVersion e) {
            log.debugv("Version of Jandex index at {} is not supported", indexUrlString);
        } catch (FileNotFoundException ignore) {
            // There is no index available.
            log.tracev("No Jandex index found at {}", indexUrlString);
        } catch (IOException ioe) {
            log.debugv("Cannot load Jandex index at {}", indexUrlString);
            log.trace("", ioe);
        } finally {
            if (indexFileStream != null) {
                try {
                    indexFileStream.close();
                } catch (IOException ioe) {
                    WeldSELogger.LOG.couldNotCloseStreamOfJandexIndex(urlPath, ioe);
                }
            }
        }

        return index;
    }

    private String getJandexIndexURLString(final String urlPath) {
        String indexUrlString = FILE_URL_PREFIX + urlPath + SEPARATOR + AbstractWeldSEDeployment.JANDEX_INDEX_NAME;
        if (urlPath.toLowerCase().endsWith(".jar")) {
            indexUrlString = JAR_URL_PREFIX + indexUrlString;
        }

        return indexUrlString;
    }

    private void handleArchiveByIndex(Index index) {
        for (ClassInfo classInfo : index.getKnownClasses()) {
            discoveredClasses.add(classInfo.name().toString());
        }
    }

    private BeanArchiveBuilder createBeanArchiveBuilder(Index index) {
        return new BeanArchiveBuilder(null, index, discoveredClasses, beansXml);
    }
}
