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

import javax.servlet.ServletContext;

import org.jboss.weld.environment.servlet.Container;
import org.jboss.weld.environment.servlet.ContainerContext;
import org.jboss.weld.environment.servlet.logging.JettyLogger;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * Jetty &gt;= 9.4.20 container.
 * <p>This container requires that the jetty server register DecoratingListener
 * to dynamically register a decorator instance that wraps the {@link WeldDecorator}
 * added as an attribute.   The jetty 'cdi' module does this and indicates it's
 * availability by setting the "org.eclipse.jetty.cdi" attribute to "DecoratingListener"
 * </p>
 *
 * @see JettyLegacyContainer
 * @author <a href="mailto:gregw@webtide.com">Greg Wilkins</a>
 */
public class JettyContainer extends AbstractJettyContainer {

    public static final Container INSTANCE = new JettyContainer();
    public static final String JETTY_CDI_ATTRIBUTE = "org.eclipse.jetty.cdi";
    public static final String JETTY_CDI_ATTRIBUTE_VALUE = "DecoratingListener";

    protected String classToCheck() {
        // Never called because touch is overridden below.
        throw new UnsupportedOperationException("touch method reimplemented in JettyContainer");
    }

    @Override
    public boolean touch(ResourceLoader resourceLoader, ContainerContext context) throws Exception {
        ServletContext sc = context.getServletContext();
        // The jetty cdi module from 9.4.20 sets this attribute to indicate that a DecoratingListener is registered.
        return JETTY_CDI_ATTRIBUTE_VALUE.equals(sc.getAttribute(JETTY_CDI_ATTRIBUTE));
    }

    @Override
    public void initialize(ContainerContext context) {
        // Try pushing a Jetty Injector into the servlet context
        try {
            super.initialize(context);
            WeldDecorator.process(context.getServletContext());
            JettyLogger.LOG.jettyCDIDetectedInjectionIsSupported();
        } catch (Exception e) {
            JettyLogger.LOG.unableToCreateJettyWeldInjector(e);
        }
    }
}