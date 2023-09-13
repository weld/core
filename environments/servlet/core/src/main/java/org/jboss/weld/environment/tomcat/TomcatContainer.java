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

package org.jboss.weld.environment.tomcat;

import org.jboss.weld.environment.servlet.AbstractContainer;
import org.jboss.weld.environment.servlet.Container;
import org.jboss.weld.environment.servlet.ContainerContext;
import org.jboss.weld.environment.servlet.EnhancedListener;
import org.jboss.weld.environment.servlet.logging.TomcatLogger;

/**
 * Tomcat 7.x and 8.x container.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TomcatContainer extends AbstractContainer {

    public static final Container INSTANCE = new TomcatContainer();

    private static final String TOMCAT_REQUIRED_CLASS_NAME = "org.apache.catalina.connector.Request";

    protected String classToCheck() {
        return TOMCAT_REQUIRED_CLASS_NAME;
    }

    public void initialize(ContainerContext context) {
        try {
            WeldForwardingInstanceManager.replaceInstanceManager(context.getServletContext(), context.getManager());
            if (Boolean.TRUE
                    .equals(context.getServletContext().getAttribute(EnhancedListener.ENHANCED_LISTENER_USED_ATTRIBUTE_NAME))) {
                TomcatLogger.LOG.allInjectionsAvailable();
            } else {
                TomcatLogger.LOG.listenersInjectionsNotAvailable();
            }
        } catch (Exception e) {
            TomcatLogger.LOG.unableToReplaceTomcat(e);
        }
    }
}
