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
 * Jetty 7.2+, 8.x and 9.x container.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JettyContainer extends AbstractJettyContainer {

    public static final Container INSTANCE = new JettyContainer();

    protected String classToCheck() {
        // Never called because touch is overridden below.
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean touch(ResourceLoader resourceLoader, ContainerContext context) throws Exception {
        ServletContext sc = context.getServletContext();
        return "DecoratingListener".equals(sc.getAttribute("org.eclipse.jetty.cdi"));
    }

    @Override
    public void initialize(ContainerContext context) {
        // Try pushing a Jetty Injector into the servlet context
        try {
            context.getServletContext().setAttribute(INJECTOR_ATTRIBUTE_NAME, new JettyWeldInjector(context.getManager()));
            WeldDecorator.process(context.getServletContext());
            JettyLogger.LOG.jettyCDIDetectedInjectionIsSupported();
        } catch (Exception e) {
            JettyLogger.LOG.unableToCreateJettyWeldInjector(e);
        }
    }
}