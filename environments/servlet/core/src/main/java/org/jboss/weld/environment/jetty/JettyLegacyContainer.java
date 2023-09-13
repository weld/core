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

import org.jboss.weld.environment.servlet.Container;
import org.jboss.weld.environment.servlet.ContainerContext;
import org.jboss.weld.environment.servlet.EnhancedListener;
import org.jboss.weld.environment.servlet.logging.JettyLogger;

/**
 * Legacy Jetty container.
 * <p>
 * This container relies on the the following Jetty APIs to be exposed
 * to the webapp:
 * <ul>
 * <li>org.eclipse.jetty.server.handler.ContextHandler</li>
 * <li>org.eclipse.jetty.servlet.ServletContextHandler</li>
 * <li>org.eclipse.jetty.util.DecoratedObjectFactory</li>
 * <li>org.eclipse.jetty.util.Decorator</li>
 * </ul>
 * These APIs are exposed by the deprecated jetty <code>cdi2</code> module.
 * </p>
 *
 * @deprecated
 * @see JettyContainer
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Deprecated
public class JettyLegacyContainer extends AbstractJettyContainer {

    public static final Container INSTANCE = new JettyLegacyContainer();

    private static final String JETTY_REQUIRED_CLASS_NAME = "org.eclipse.jetty.util.Decorator";

    protected String classToCheck() {
        return JETTY_REQUIRED_CLASS_NAME;
    }

    @Override
    public void initialize(ContainerContext context) {
        // Try pushing a Jetty Injector into the servlet context
        try {
            super.initialize(context);
            LegacyWeldDecorator.process(context.getServletContext());
            if (Boolean.TRUE
                    .equals(context.getServletContext().getAttribute(EnhancedListener.ENHANCED_LISTENER_USED_ATTRIBUTE_NAME))) {
                // ServletContainerInitializer works on versions prior to 9.1.1 but the listener injection doesn't
                JettyLogger.LOG.jettyDetectedListenersInjectionIsSupported();
            } else {
                JettyLogger.LOG.jettyDetectedListenersInjectionIsNotSupported();
            }
        } catch (Exception e) {
            JettyLogger.LOG.unableToCreateJettyWeldInjector(e);
        }
    }
}