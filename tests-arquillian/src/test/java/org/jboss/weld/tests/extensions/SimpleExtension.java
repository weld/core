/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.el.ELAwareBeanManager;

import org.jboss.weld.manager.api.WeldManager;

public class SimpleExtension implements Extension {

    private static boolean observedBeforeBeanDiscovery;
    public static boolean observedBeforeBeanDiscoveryBc;
    public static boolean observedBeforeBeanDiscoveryBm;
    public static boolean observedBeforeBeanDiscoveryWm;
    public static boolean observedBeforeBeanDiscoveryElBm;

    public static boolean isObservedBeforeBeanDiscovery() {
        return observedBeforeBeanDiscovery;
    }
    
    public void observeBeforeShutdown(@Observes BeforeShutdown beforeShutdown, ELAwareBeanManager beanManager) {
        assert beanManager != null;
        assert beanManager.getELResolver() != null;
    }

    public void observeBeanManager(@Observes BeforeBeanDiscovery event) {
        observedBeforeBeanDiscovery = true;
    }

    public void observeBeanManager(@Observes BeforeBeanDiscovery event, BeanManager bm) {
        observedBeforeBeanDiscoveryBm = true;
        assert bm != null;
    }

    public void observeBeanManager(@Observes BeforeBeanDiscovery event, BeanContainer bc) {
        observedBeforeBeanDiscoveryBc = true;
        assert bc != null;
    }

    public void observeBeanManager(@Observes BeforeBeanDiscovery event, WeldManager wm) {
        observedBeforeBeanDiscoveryWm = true;
        assert wm != null;
    }

    public void observeBeanManager(@Observes BeforeBeanDiscovery event, ELAwareBeanManager bm) {
        observedBeforeBeanDiscoveryElBm = true;
        assert bm != null;
    }

}
