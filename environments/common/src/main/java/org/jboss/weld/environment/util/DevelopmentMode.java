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
package org.jboss.weld.environment.util;

import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * Development mode utils.
 *
 * @author Martin Kouba
 */
public final class DevelopmentMode {

    public static final String PROBE_EXTENSION_CLASS_NAME = "org.jboss.weld.probe.ProbeExtension";

    public static final String PROBE_FILTER_CLASS_NAME = "org.jboss.weld.probe.ProbeFilter";

    private DevelopmentMode() {
    }

    /**
     *
     * @param resourceLoader
     * @return a new instance of Probe extension
     */
    public static Extension getProbeExtension(ResourceLoader resourceLoader) {
        try {
            Class<? extends Extension> probeExtensionClass = Reflections.loadClass(resourceLoader, PROBE_EXTENSION_CLASS_NAME);
            if (probeExtensionClass == null) {
                throw CommonLogger.LOG.probeComponentNotFoundOnClasspath(PROBE_EXTENSION_CLASS_NAME);
            }
            return SecurityActions.newInstance(probeExtensionClass);
        } catch (Exception e) {
            throw CommonLogger.LOG.unableToInitializeProbeComponent(e.getMessage(), e);
        }
    }

}
