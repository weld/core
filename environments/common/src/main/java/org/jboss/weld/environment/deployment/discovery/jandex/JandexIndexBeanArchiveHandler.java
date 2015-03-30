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
package org.jboss.weld.environment.deployment.discovery.jandex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.UnsupportedVersion;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.util.Preconditions;

/**
 * This class uses a Jandex-Index ("META-INF/jandex.idx") to scan the the archive. If no index is available the {@link JandexIndexBeanArchiveHandler#handle(String)}
 * method will return null. To prevent this, use {@link JandexIndexBeanArchiveHandler#canHandle(String)} to check if an index is available and supported.
 * <p>
 * The class is not thread-safe and should only be used by a single thread
 *
 * @author Stefan Großmann
 */
public class JandexIndexBeanArchiveHandler implements BeanArchiveHandler {
    private static final String JANDEX_INDEX_NAME = "META-INF/jandex.idx";

    private static final String JAR_URL_PREFIX = "jar:";
    private static final String FILE_URL_PREFIX = "file:";
    private static final String SEPARATOR = "!/";

    private Index indexCache = null;
    private String indexCacheUrlPath = null;

    public boolean canHandle(String urlPath) {
        return getIndex(urlPath) != null;
    }

    @Override
    public BeanArchiveBuilder handle(String urlPath) {
        Index index = getIndex(urlPath);
        if (index == null) {
            return null;
        }

        BeanArchiveBuilder builder = new BeanArchiveBuilder().setAttribute(JandexDiscoveryStrategy.INDEX_ATTRIBUTE_NAME, index);
        handleArchiveByIndex(index, builder);
        return builder;
    }

    private Index getIndex(final String urlPath) {
        Preconditions.checkArgumentNotNull(urlPath, "urlPath");

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
            CommonLogger.LOG.foundJandexIndex(indexURL);
            final IndexReader indexFileReader = new IndexReader(indexFileStream);
            index = indexFileReader.read();
        } catch (IllegalArgumentException e) {
            CommonLogger.LOG.warnv("Jandex index at {} is not valid", indexUrlString);
        } catch (UnsupportedVersion e) {
            CommonLogger.LOG.warnv("Version of Jandex index at {} is not supported", indexUrlString);
        } catch (FileNotFoundException ignore) {
            // There is no index available.
            CommonLogger.LOG.tracev("No Jandex index found at {}", indexUrlString);
        } catch (IOException ioe) {
            CommonLogger.LOG.warnv("Cannot load Jandex index at {}", indexUrlString);
            CommonLogger.LOG.catchingDebug(ioe);
        } finally {
            if (indexFileStream != null) {
                try {
                    indexFileStream.close();
                } catch (IOException ioe) {
                    CommonLogger.LOG.couldNotCloseStreamOfJandexIndex(urlPath, ioe);
                }
            }
        }

        return index;
    }

    private String getJandexIndexURLString(final String urlPath) {
        String indexUrlString = FILE_URL_PREFIX + urlPath + SEPARATOR + JANDEX_INDEX_NAME;
        if (new File(urlPath).isFile()) {
            indexUrlString = JAR_URL_PREFIX + indexUrlString;
        }

        return indexUrlString;
    }

    private void handleArchiveByIndex(Index index, BeanArchiveBuilder builder) {
        for (ClassInfo classInfo : index.getKnownClasses()) {
            builder.addClass(classInfo.name().toString());
        }
    }
}
