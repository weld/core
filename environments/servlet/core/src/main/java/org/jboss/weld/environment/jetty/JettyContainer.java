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
import org.jboss.weld.environment.servlet.EnhancedListener;
import org.jboss.weld.environment.servlet.logging.JettyLogger;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * Jetty 7.2+, 8.x and 9.x container.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JettyContainer extends AbstractJettyContainer {

    public static final Container INSTANCE = new JettyContainer();

    private static final String JETTY_SERVERNAME = "jetty";

    private static final String JETTY_REQUIRED_CLASS_NAME = "org.eclipse.jetty.servlet.ServletHandler";

    private static final int MAJOR_VERSION = 7;
    private static final int MINOR_VERSION = 2;

    protected String classToCheck() {
        // This is not used anyway - server implementation classes are hidden from the web application in Jetty by default
        // Instead ServletContext.getServerInfo() is parsed
        return JETTY_REQUIRED_CLASS_NAME;
    }

    @Override
    public boolean touch(ResourceLoader resourceLoader, ContainerContext context) throws Exception {
        ServletContext sc = context.getServletContext();
        String si = sc.getServerInfo();
        JettyLogger.LOG.debugv("Parsing server info: {0}", si);
        if(!si.contains(JETTY_SERVERNAME)) {
            return false;
        }
        int p = si.indexOf("/");
        if (p < 0) {
            return false;
        }
        String version = si.substring(p + 1);
        String[] split = version.split("\\.");
        int major = parseVersion(split[0]);
        int minor = parseVersion(split[1]);
        return (major > MAJOR_VERSION || (major == MAJOR_VERSION & minor >= MINOR_VERSION));
    }

    @Override
    public void initialize(ContainerContext context) {
        // Try pushing a Jetty Injector into the servlet context
        try {
            context.getServletContext().setAttribute(INJECTOR_ATTRIBUTE_NAME, new JettyWeldInjector(context.getManager()));
            WeldDecorator.process(context.getServletContext());
            if(Boolean.TRUE.equals(context.getServletContext().getAttribute(EnhancedListener.ENHANCED_LISTENER_USED_ATTRIBUTE_NAME))) {
                // ServletContainerInitializer works on versions prior to 9.1.1 but the listener injection doesn't
                JettyLogger.LOG.jettyDetectedListenersInjectionIsSupported();
            } else {
                JettyLogger.LOG.jettyDetectedListenersInjectionIsNotSupported();
            }
        } catch (Exception e) {
            JettyLogger.LOG.unableToCreateJettyWeldInjector(e);
        }
    }

    private int parseVersion(String version) {
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException e) {
            JettyLogger.LOG.debugv("Unable to parse version string: {0}", version);
            return -1;
        }
    }

}