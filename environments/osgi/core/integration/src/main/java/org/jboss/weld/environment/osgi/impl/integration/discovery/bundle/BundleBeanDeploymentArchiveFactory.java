/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.osgi.impl.integration.discovery.bundle;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.environment.osgi.impl.integration.discovery.BundleBeanDeploymentArchive;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Factory for {@link BeanDeploymentArchive} used by {@link BundleDeployment}.
 * <p/>
 * It scans bundle in order to verify it is a CDI manageable bundle and to
 * create a complete {@link BeanDeploymentArchive}.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class BundleBeanDeploymentArchiveFactory {
    private Logger logger = LoggerFactory.getLogger(
            BundleBeanDeploymentArchiveFactory.class);

    private Set<String> discoveredClasses = new HashSet<String>();

    private List<URL> discoveredBeanXmlUrls = new ArrayList<URL>();

    private Bundle bundle = null;

    public BeanDeploymentArchive scan(Bundle bundle, Bootstrap bootstrap) {
        logger.debug("Scanning bundle {}", bundle);

        this.bundle = bundle;
        discoveredBeanXmlUrls.clear();
        discoveredClasses.clear();

        Enumeration beansXmlMarkers = bundle.findEntries("/", "beans.xml", true);
        Enumeration innerJars = bundle.findEntries("/", "*.jar", true);
        Enumeration innerZips = bundle.findEntries("/", "*.zip", true);

        if (beansXmlMarkers != null) {
            scanRoot(beansXmlMarkers);
        }
        if (innerZips != null) {
            while (innerZips.hasMoreElements()) {
                scanZip((URL) innerZips.nextElement());
            }
        }
        if (innerJars != null) {
            while (innerJars.hasMoreElements()) {
                scanZip((URL) innerJars.nextElement());
            }
        }

        if (discoveredBeanXmlUrls.size() < 1) {
            logger.debug("No beans.xml file found for bundle {}", bundle);
            return null;
        }

        logger.debug("Creation of a BundleBeanDeploymentArchive for bundle {}",
                bundle);
        BundleBeanDeploymentArchive archive =
                new BundleBeanDeploymentArchive("bundle-bean-deployment-archive-"
                        + bundle.getBundleId());
        archive.setBeansXml(bootstrap.parse(discoveredBeanXmlUrls));
        archive.setBeanClasses(discoveredClasses);

        return archive;
    }

    private void scanZip(URL zipUrl) {
        ZipInputStream zipInputStream = null;
        ZipEntry zipEntry = null;
        String zipPath = zipUrl.getPath();
        String zipEntryPath = "";
        Set<String> zipClasses = new HashSet<String>();
        List<URL> zipBeanXmlUrls = new ArrayList<URL>();
        try {
            logger.trace("Scanning zip file {} for bean classes", zipPath);
            zipInputStream = new ZipInputStream(zipUrl.openStream());
            zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                zipEntryPath = "/" + zipEntry.getName();
                if (zipEntryPath.toLowerCase().endsWith(".zip")
                        || zipEntryPath.toLowerCase().endsWith(".jar")) {
                    logger.trace("Found an inner zip file {} within zip file {}",
                            zipEntryPath,
                            zipPath);
                    scanZip(new URL("jar:" + zipUrl + "!" + zipEntryPath));
                } else if (zipEntryPath.toLowerCase().endsWith(".class")) {
                    String clazz = null;
                    String[] parts = zipEntryPath.split("!");
                    if (parts.length > 1) {
                        clazz = parts[1].substring(1).
                            replace("/", ".").
                            replace(".class", "");
                    } else {
                        clazz = zipEntryPath.substring(1).
                            replace("/", ".").
                            replace(".class", "");
                    }
                    logger.trace("Found a new bean class: {}", clazz);
                    zipClasses.add(clazz);
                } else if (zipEntryPath.toLowerCase().endsWith("beans.xml")) {
//                    if (!zipEntryPath.equalsIgnoreCase("/meta-inf/beans.xml")) {
//                        logger.warn("Invalid location for beans.xml file: {}",
//                                zipEntryPath);
//                    } else {
                    logger.trace("Found a new beans.xml file: {}",
                            zipEntryPath);
                    zipBeanXmlUrls.add(new URL("jar:" + zipUrl + "!"
                            + zipEntryPath));
//                    }

                }
                zipEntry = zipInputStream.getNextEntry();
            }
            if (zipBeanXmlUrls.size() > 1) {
                discoveredBeanXmlUrls.addAll(zipBeanXmlUrls);
                discoveredClasses.addAll(zipClasses);
            }
        } catch (MalformedURLException e) {
            logger.error("Malformed URL", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.warn("The zip file (or one of its entries) {} "
                    + "was inaccessible: {}",
                    zipUrl,
                    e);
        }
    }

    private void scanRoot(Enumeration beansXmlMarkers) {
        while (beansXmlMarkers.hasMoreElements()) {
            URL beansXmlUrl = (URL) beansXmlMarkers.nextElement();
            String beansXmlPath = beansXmlUrl.getPath();
            logger.trace("Found a new beans.xml file: {}", beansXmlPath);
            if (!beansXmlPath.endsWith("META-INF/beans.xml")) {
                logger.warn("Invalid location for beans.xml file: {}", beansXmlPath);
                continue;
            }
            discoveredBeanXmlUrls.add(beansXmlUrl);
            logger.trace("Scanning bundle {} for bean classes", bundle);
            Enumeration beanClasses = bundle.findEntries("/", "*.class", true);
            if (beanClasses != null) {
                while (beanClasses.hasMoreElements()) {
                    URL url = (URL) beanClasses.nextElement();
                    String clazz = null;
                    String[] parts = url.getFile().split("!");
                    if (parts.length > 1) {
                        clazz = parts[1].substring(1).
                            replace("/", ".").
                            replace(".class", "");
                    } else {
                        clazz = url.getFile().substring(1).
                            replace("/", ".").
                            replace(".class", "");
                    }
//                    String clazz = url.getFile().substring(1).
//                            replace("/", ".").
//                            replace(".class", "");
                    logger.trace("Found a new bean class: {}", clazz);
                    discoveredClasses.add(clazz);
                }
            }
        }
    }

}
