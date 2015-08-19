/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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

import static org.jboss.weld.environment.util.URLUtils.JAR_URL_SEPARATOR;
import static org.jboss.weld.environment.util.URLUtils.PROCOTOL_JAR;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.enterprise.inject.spi.Extension;

import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.deployment.AbstractWeldDeployment;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.security.GetSystemPropertyAction;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Scans all the class path entries. Implicit bean archives which don't contain a beans.xml file are also supported.
 *
 * @author Martin Kouba
 * @see ConfigurationKey#IMPLICIT_SCAN
 */
public class ClassPathBeanArchiveScanner extends AbstractBeanArchiveScanner {

    static final String JAVA_CLASS_PATH_SYSTEM_PROPERTY = "java.class.path";

    static final String EXTENSION_FILE = "META-INF/" + Extension.class.getName();

    private static final Logger logger = Logger.getLogger(ClassPathBeanArchiveScanner.class);

    private static final String BEANS_XML_FOUND_MESSAGE = "beans.xml found in {0}";

    private static final String BEANS_XML_NOT_FOUND_MESSAGE = "beans.xml not found in {0}";

    /**
     *
     * @param bootstrap
     */
    public ClassPathBeanArchiveScanner(Bootstrap bootstrap) {
        super(bootstrap);
    }

    @Override
    public List<ScanResult> scan() {
        String javaClassPath = AccessController.doPrivileged(new GetSystemPropertyAction(JAVA_CLASS_PATH_SYSTEM_PROPERTY));
        if (javaClassPath == null) {
            throw CommonLogger.LOG.cannotReadJavaClassPathSystemProperty();
        }
        ImmutableList.Builder<ScanResult> results = ImmutableList.builder();
        Set<String> entries = ImmutableSet.of(javaClassPath.split(Pattern.quote(File.pathSeparator)));
        logger.debugv("Scanning class path entries: {0}", entries);
        for (String entry : entries) {
            if (entry == null || entry.isEmpty()) {
                continue;
            }
            File entryFile = new File(entry);
            if (!entryFile.canRead()) {
                throw CommonLogger.LOG.cannotReadClassPathEntry(entryFile);
            }
            try {
                if (entryFile.isDirectory()) {
                    scanDirectory(entryFile, results);
                } else {
                    scanJarFile(entryFile, results);
                }
            } catch (IOException e) {
                throw CommonLogger.LOG.cannotScanClassPathEntry(entryFile, e);
            }
        }
        return results.build();
    }

    private void scanDirectory(File entryDirectory, ImmutableList.Builder<ScanResult> results) throws IOException {
        // First try to find beans.xml
        File beansXmlFile = new File(entryDirectory, AbstractWeldDeployment.BEANS_XML);
        if (beansXmlFile.canRead()) {
            logger.debugv(BEANS_XML_FOUND_MESSAGE, entryDirectory);
            final BeansXml beansXml = parseBeansXml(beansXmlFile.toURI().toURL());
            if (accept(beansXml)) {
                results.add(new ScanResult(beansXml, entryDirectory.getPath()));
            }
        } else {
            // No beans.xml found - check whether the bean archive contains an extension
            logger.debugv(BEANS_XML_NOT_FOUND_MESSAGE, entryDirectory);
            File extensionFile = new File(entryDirectory, EXTENSION_FILE);
            if (!extensionFile.canRead()) {
                results.add(new ScanResult(null, entryDirectory.getPath()));
            }
        }
    }

    private void scanJarFile(File entryFile, ImmutableList.Builder<ScanResult> results) throws IOException {
        try (ZipFile zip = new ZipFile(entryFile)) {
            ZipEntry beansXmlEntry = zip.getEntry(AbstractWeldDeployment.BEANS_XML);
            if (beansXmlEntry != null) {
                logger.debugv(BEANS_XML_FOUND_MESSAGE, entryFile);
                BeansXml beansXml = parseBeansXml(
                        new URL(PROCOTOL_JAR + ":" + entryFile.toURI().toURL().toExternalForm() + JAR_URL_SEPARATOR + beansXmlEntry.getName()));
                if (accept(beansXml)) {
                    results.add(new ScanResult(beansXml, entryFile.getPath()));
                }
            } else {
                // No beans.xml found - check whether the bean archive contains an extension
                if (zip.getEntry(EXTENSION_FILE) == null) {
                    logger.debugv(BEANS_XML_NOT_FOUND_MESSAGE, entryFile);
                    results.add(new ScanResult(null, entryFile.getPath()));
                }
            }
        }
    }

}
