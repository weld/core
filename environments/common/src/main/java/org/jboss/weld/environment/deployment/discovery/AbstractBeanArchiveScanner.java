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

import java.net.URL;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;

/**
 *
 * @author Martin Kouba
 */
public abstract class AbstractBeanArchiveScanner implements BeanArchiveScanner {

    protected final Bootstrap bootstrap;
    // Allow to treat empty beans.xml as having discovery mode other than default (which is ANNOTATED from CDI 4.0)
    protected final BeanDiscoveryMode emptyBeansXmlDiscoveryMode;

    /**
     *
     * @param bootstrap
     */
    public AbstractBeanArchiveScanner(Bootstrap bootstrap, BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        this.bootstrap = bootstrap;
        this.emptyBeansXmlDiscoveryMode = emptyBeansXmlDiscoveryMode;
    }

    protected boolean accept(BeansXml beansXml) {
        return !BeanDiscoveryMode.NONE.equals(beansXml.getBeanDiscoveryMode());
    }

    protected BeansXml parseBeansXml(URL beansXmlUrl) {
        return bootstrap.parse(beansXmlUrl, emptyBeansXmlDiscoveryMode);
    }

}
