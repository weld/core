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
package org.jboss.weld.environment.osgi.impl.embedded;

import org.jboss.weld.environment.osgi.impl.WeldCDIContainer;
import org.jboss.weld.environment.se.WeldContainer;
import org.osgi.framework.BundleContext;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;

/**
 * Embedded Weld container used for bean bundles that are not managed by
 * Weld-OSGi directly.
 * <p/>
 * It is responsible for initialization of a Weld container requested by the
 * bean bundles.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class WeldOSGi extends org.jboss.weld.environment.se.Weld {
    private WeldCDIContainer container;

    public WeldOSGi(BundleContext context) {
        super();
        container = new WeldCDIContainer(context.getBundle());
    }

    public WeldContainer initialize() {
        container.initialize();
        return new WeldOSGiContainer(this);
    }

    public void shutdown() {
        container.shutdown();
    }

    public static class WeldOSGiContainer extends WeldContainer {
        private final WeldOSGi weld;

        public WeldOSGiContainer(WeldOSGi weld) {
            super(null, null);
            this.weld = weld;
        }

        @Override
        public Instance<Object> instance() {
            return weld.container.getInstance();
        }

        @Override
        public Event<Object> event() {
            return weld.container.getEvent();
        }

        @Override
        public BeanManager getBeanManager() {
            return weld.container.getBeanManager();
        }

    }
}
