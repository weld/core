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

import org.jboss.weld.environment.servlet.Container;
import org.jboss.weld.environment.servlet.ContainerContext;
import org.jboss.weld.environment.servlet.logging.JettyLogger;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * Jetty Container.
 * <p>
 * This container requires that the jetty server register DecoratingListener
 * to dynamically register a decorator instance that wraps the {@link WeldDecorator}
 * added as an attribute. The jetty <code>decorate</code> module does this and indicates it's
 * availability by setting the "org.eclipse.jetty.webapp.DecoratingListener" to the
 * name of the watched attribute.
 * </p>
 *
 * <p>
 * Jetty also provides the <code>cdi-spi</code> module that may directly invoke the
 * CDI SPI. This module indicates it's availability by setting the "org.eclipse.jetty.cdi"
 * context attribute to "CdiDecorator". If this module is used, then this JettyContainer
 * only logs a message and does no further integration.
 * </p>
 *
 * @since Jetty 9.4.20
 * @see JettyLegacyContainer
 * @author <a href="mailto:gregw@webtide.com">Greg Wilkins</a>
 */
public class JettyContainer extends AbstractJettyContainer {
    public static final Container INSTANCE = new JettyContainer();
    public static final String JETTY_CDI_ATTRIBUTE = "org.eclipse.jetty.cdi";
    public static final String CDI_SPI_DECORATOR_MODE = "CdiSpiDecorator";
    public static final String CDI_DECORATING_LISTENER_MODE = "CdiDecoratingListener";
    public static final String CDI_DECORATING_LISTENER_ATTRIBUTE = "org.eclipse.jetty.cdi.decorator";
    public static final String DECORATING_LISTENER_MODE = "DecoratingListener";
    public static final String DECORATING_LISTENER_ATTRIBUTE = "org.eclipse.jetty.webapp.DecoratingListener";

    protected String classToCheck() {
        // Never called because touch is overridden below.
        throw new UnsupportedOperationException("touch method reimplemented in JettyContainer");
    }

    @Override
    public boolean touch(ResourceLoader resourceLoader, ContainerContext context) throws Exception {
        ServletContext servletContext = context.getServletContext();
        // The jetty cdi modules from 9.4.20 sets JETTY_CDI_ATTRIBUTE to indicate that a
        // DecoratingListener is registered. Weld-3.1.2.Final documented that the decorate module
        // could be used directly, which sets DECORATING_LISTENER_ATTRIBUTE
        return servletContext.getAttribute(JETTY_CDI_ATTRIBUTE) instanceof String ||
                servletContext.getAttribute(DECORATING_LISTENER_ATTRIBUTE) instanceof String;
    }

    @Override
    public void initialize(ContainerContext context) {
        try {
            ServletContext servletContext = context.getServletContext();
            String mode = (String) servletContext.getAttribute(JETTY_CDI_ATTRIBUTE);
            if (mode == null) {
                mode = DECORATING_LISTENER_MODE;
            }
            switch (mode) {
                case CDI_SPI_DECORATOR_MODE:
                    // For use with the cdi-spi module
                    // No further integration required
                    JettyLogger.LOG.jettyCdiSpiIsSupported();
                    break;

                case CDI_DECORATING_LISTENER_MODE:
                    // For use with the cdi-decorate module
                    // Initialize a JettyWeldInjector and create WeldDecorator for it
                    super.initialize(context);
                    servletContext.setAttribute(CDI_DECORATING_LISTENER_ATTRIBUTE, new WeldDecorator(servletContext));
                    JettyLogger.LOG.jettyCdiDecorationIsSupported();
                    break;

                case DECORATING_LISTENER_MODE:
                    // For use with the decorate module
                    // This mode is only needed to match the Weld-3.1.2 documentation.
                    // Initialize a JettyWeldInjector and create WeldDecorator for it
                    super.initialize(context);
                    String attribute = (String) servletContext.getAttribute(DECORATING_LISTENER_ATTRIBUTE);
                    servletContext.setAttribute(attribute, new WeldDecorator(servletContext));
                    JettyLogger.LOG.jettyDecorationIsSupported();
                    break;

                default:
                    throw JettyLogger.LOG.unknownIntegrationMode(mode);
            }
        } catch (Exception e) {
            JettyLogger.LOG.unableToCreateJettyWeldInjector(e);
        }
    }
}