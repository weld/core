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

import java.net.URL;
import java.util.Map;

import org.jboss.weld.bootstrap.spi.BeansXml;

/**
 * Scans the application for bean archives.
 *
 * The implementation may be optimized for bean archives containing beans.xml file with bean-discovey-mode of none. E.g. it does not have to scan classes in
 * such an archive.
 *
 * @author Martin Kouba
 */
public interface BeanArchiveScanner {
    public static class ScanResult {
        private final String beanArchiveRef;
        private final BeansXml beansXml;

        public ScanResult(final BeansXml beansXml, final String beanArchiveRef) {
            this.beansXml = beansXml;
            this.beanArchiveRef = beanArchiveRef;
        }

        public String getBeanArchiveRef() {
            return beanArchiveRef;
        }

        public BeansXml getBeansXml() {
            return beansXml;
        }
    }

    /**
     * Scans for bean archives identified by beans.xml files. The map must not contain multiple results with the same {@link ScanResult#beanArchiveRef}.
     *
     * @return the map of {@link ScanResult} representations mapped by url of the descriptor
     */
    Map<URL, ScanResult> scan();
}
