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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.UnsupportedVersion;
import org.jboss.logging.Logger;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.util.Preconditions;

import static org.jboss.weld.environment.util.URLUtils.PROTOCOL_JRT_PART;

/**
 * This class uses an existing Jandex-Index ("META-INF/jandex.idx") to scan the bean archive. If no index is available the
 * {@link JandexIndexBeanArchiveHandler#handle(String)} method will return null.
 *
 * <p>
 * The class is not thread-safe and should only be used by a single thread.
 * </p>
 *
 * @author Stefan Großmann
 */
public class JandexIndexBeanArchiveHandler implements BeanArchiveHandler {

    private static final Logger logger = Logger.getLogger(JandexIndexBeanArchiveHandler.class);

    private static final String JANDEX_INDEX_NAME = "META-INF/jandex.idx";

    @Override
    public BeanArchiveBuilder handle(String path) {
        Index index;
        if (path.startsWith(PROTOCOL_JRT_PART)) {
            index = getIndexFromJrt(path);
        } else {
            File beanArchiveFile = new File(path);
            if (!beanArchiveFile.canRead() || beanArchiveFile.isDirectory()) {
                // Currently only JAR files are supported
                return null;
            }
            index = getIndexFromJar(beanArchiveFile);
        }
        if (index == null) {
            return null;
        }
        BeanArchiveBuilder builder = new BeanArchiveBuilder().setAttribute(Jandex.INDEX_ATTRIBUTE_NAME, index);
        handleArchiveByIndex(index, builder);
        return builder;
    }

    private Index getIndexFromJrt(String jrtPath) {
        logger.debugv("Try to get Jandex index from JRT: {0}", jrtPath);
        Index index = null;
        try {
            URL url = URI.create(jrtPath + "/" + JANDEX_INDEX_NAME).toURL();
            index = new IndexReader(url.openStream()).read();
            logger.debugv("Jandex index found: {0}", jrtPath);
        } catch (IOException e) {
            CommonLogger.LOG.catchingDebug(e);
        } finally {
            CommonLogger.LOG.errorv("Cannot get Jandex index from JRT: {0}", jrtPath);
        }
        return index;
    }

    private Index getIndexFromJar(File beanArchiveFile) {
        Preconditions.checkArgumentNotNull(beanArchiveFile, "beanArchiveFile");
        logger.debugv("Try to get Jandex index for: {0}", beanArchiveFile);
        Index index = null;
        try (ZipFile zip = new ZipFile(beanArchiveFile)) {
            // Open the bean archive and try to find the index file
            ZipEntry entry = zip.getEntry(JANDEX_INDEX_NAME);
            if (entry != null) {
                index = new IndexReader(zip.getInputStream(entry)).read();
            }
        } catch (IllegalArgumentException e) {
            CommonLogger.LOG.warnv("Jandex index is not valid: {0}", beanArchiveFile);
        } catch (UnsupportedVersion e) {
            CommonLogger.LOG.warnv("Version of Jandex index is not supported: {0}", beanArchiveFile);
        } catch (IOException e) {
            CommonLogger.LOG.warnv("Cannot get Jandex index from: {0}", beanArchiveFile);
            CommonLogger.LOG.catchingDebug(e);
        }
        logger.debugv("Jandex index {0}found: {1}", index == null ? "NOT " : "", beanArchiveFile);
        return index;
    }

    private void handleArchiveByIndex(Index index, BeanArchiveBuilder builder) {
        for (ClassInfo classInfo : index.getKnownClasses()) {
            builder.addClass(classInfo.name().toString());
        }
    }
}
