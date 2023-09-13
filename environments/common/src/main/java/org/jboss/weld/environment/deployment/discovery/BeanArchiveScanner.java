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

import java.util.List;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.util.BeanArchives;

/**
 * Scans the application for bean archives.
 *
 * The implementation may be optimized for bean archives containing beans.xml file with bean-discovey-mode of none. E.g. it does
 * not have to scan classes in
 * such an archive.
 *
 * @author Martin Kouba
 */
public interface BeanArchiveScanner {

    public static class ScanResult {

        /**
         * @see BeanArchiveHandler#handle(String)
         */
        private final String beanArchiveRef;

        private final BeansXml beansXml;

        private String beanArchiveId;

        public ScanResult(BeansXml beansXml, String beanArchiveRef, String beanArchiveId) {
            this.beansXml = beansXml;
            this.beanArchiveRef = beanArchiveRef;
            this.beanArchiveId = beanArchiveId;
        }

        public ScanResult(BeansXml beansXml, String beanArchiveRef) {
            this(beansXml, beanArchiveRef, null);
        }

        public String getBeanArchiveRef() {
            return beanArchiveRef;
        }

        /**
         *
         * @return the beans.xml representation or <code>null</code> in case of a candidate for an implicit bean archive with no
         *         beans.xml
         */
        public BeansXml getBeansXml() {
            return beansXml;
        }

        /**
         * If {@link #beanArchiveRef} is not set, use {@link #beanArchiveRef}.
         *
         * @return the bean archive id to be used as {@link BeanDeploymentArchive#getId()}
         */
        public String getBeanArchiveId() {
            return beanArchiveId != null ? beanArchiveId : beanArchiveRef;
        }

        /**
         * @param base
         * @param separator
         * @return self
         * @see BeanArchives#extractBeanArchiveId(String, String, String)
         */
        public ScanResult extractBeanArchiveId(String base, String separator) {
            this.beanArchiveId = BeanArchives.extractBeanArchiveId(beanArchiveRef, base, separator);
            return this;
        }

    }

    /**
     * Scans for bean archives.
     *
     * @return an immutable list of {@link BeanArchiveScanner.ScanResult}
     */
    List<ScanResult> scan();
}
