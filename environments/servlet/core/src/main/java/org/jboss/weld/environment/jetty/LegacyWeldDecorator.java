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

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.DecoratedObjectFactory;
import org.eclipse.jetty.util.Decorator;

/**
 * Jetty Eclipse Weld support for jetty &gt;=9 and &lt;= 9.4.20
 *
 * <p>
 * This decorator relies on the the following Jetty APIs to be exposed
 * to the webapp:
 * <ul>
 * <li>org.eclipse.jetty.server.handler.ContextHandler</li>
 * <li>org.eclipse.jetty.servlet.ServletContextHandler</li>
 * <li>org.eclipse.jetty.util.DecoratedObjectFactory</li>
 * <li>org.eclipse.jetty.util.Decorator</li>
 * </ul>
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LegacyWeldDecorator extends WeldDecorator implements Decorator {

    protected LegacyWeldDecorator(ServletContext servletContext) {
        super(servletContext);
    }

    public static void process(ServletContext context) {
        if (context instanceof ContextHandler.Context) {
            ContextHandler.Context cc = (ContextHandler.Context) context;
            ContextHandler handler = cc.getContextHandler();
            if (handler instanceof ServletContextHandler) {
                ServletContextHandler sch = (ServletContextHandler) handler;
                DecoratedObjectFactory decObjFact = sch.getObjectFactory();
                decObjFact.addDecorator(new LegacyWeldDecorator(context));
            }
        }
    }
}
