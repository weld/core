/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.atinject.tck;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.Scanning;

import java.net.URL;
import java.util.Collections;
import java.util.List;

public class AllDiscoveryBeansXml implements BeansXml {
    public static final AllDiscoveryBeansXml INSTANCE = new AllDiscoveryBeansXml();

    @Override
    public List<Metadata<String>> getEnabledAlternativeStereotypes() {
        return Collections.emptyList();
    }

    @Override
    public List<Metadata<String>> getEnabledAlternativeClasses() {
        return Collections.emptyList();
    }

    @Override
    public List<Metadata<String>> getEnabledDecorators() {
        return Collections.emptyList();
    }

    @Override
    public List<Metadata<String>> getEnabledInterceptors() {
        return Collections.emptyList();
    }

    @Override
    public Scanning getScanning() {
        return Scanning.EMPTY_SCANNING;
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public BeanDiscoveryMode getBeanDiscoveryMode() {
        return BeanDiscoveryMode.ALL;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean isTrimmed() {
        return false;
    }
}
