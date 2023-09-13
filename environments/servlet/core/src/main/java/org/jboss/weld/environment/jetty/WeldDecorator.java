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

package org.jboss.weld.environment.jetty;

import jakarta.servlet.ServletContext;

import org.jboss.weld.environment.servlet.logging.JettyLogger;

/**
 * Jetty Eclipse Weld support for jetty &gt;=9.4.20
 * <p>
 * This Decorator has no hard dependencies on Jetty APIs, rather it relies on
 * the server to be configured so that an object set as the "org.eclipse.jetty.cdi.decorator"
 * is introspected for methods that match the {@link #decorate(Object)} and {@link #destroy(Object)}
 * signatures, which are then invoked dynamically.
 * </p>
 *
 * @author <a href="mailto:gregw@webtide.com">Greg Wilkins</a>
 */
public class WeldDecorator {
    private JettyWeldInjector injector;

    protected WeldDecorator(ServletContext servletContext) {
        injector = (JettyWeldInjector) servletContext.getAttribute(AbstractJettyContainer.INJECTOR_ATTRIBUTE_NAME);
        if (injector == null) {
            throw JettyLogger.LOG.noSuchJettyInjectorFound();
        }
    }

    /**
     * Decorate an object.
     * <p>
     * The signature of this method must match what is introspected for by the
     * Jetty DecoratingListener class. It is invoked dynamically.
     * </p>
     *
     * @param o The object to be decorated
     * @param <T> The type of the object to be decorated
     * @return The decorated object
     */
    public <T> T decorate(T o) {
        injector.inject(o);
        return o;
    }

    /**
     * Destroy a decorated object.
     * <p>
     * The signature of this method must match what is introspected for by the
     * Jetty DecoratingListener class. It is invoked dynamically.
     * </p>
     *
     * @param o The object to be destroyed
     */
    public void destroy(Object o) {
        injector.destroy(o);
    }
}
