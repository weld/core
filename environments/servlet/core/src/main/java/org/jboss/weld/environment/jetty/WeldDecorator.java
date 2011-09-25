/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.weld.environment.jetty;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.EventListener;

/**
 * Jetty Eclipse Weld support.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class WeldDecorator implements ServletContextHandler.Decorator {
    private ServletContext servletContext;
    private JettyWeldInjector injector;

    protected WeldDecorator(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public static void process(ServletContext context) {
        if (context instanceof ContextHandler.Context) {
            ContextHandler.Context cc = (ContextHandler.Context) context;
            ContextHandler handler = cc.getContextHandler();
            if (handler instanceof ServletContextHandler) {
                ServletContextHandler sch = (ServletContextHandler) handler;
                sch.addDecorator(new WeldDecorator(context));
            }
        }
    }

    protected JettyWeldInjector getInjector() {
        if (injector == null) {
            JettyWeldInjector jwi = (JettyWeldInjector) servletContext.getAttribute(AbstractJettyContainer.INJECTOR_ATTRIBUTE_NAME);

            if (jwi == null)
                throw new IllegalArgumentException("No such Jetty injector found in servlet context attributes.");

            injector = jwi;
        }

        return injector;
    }

    public <T extends Filter> T decorateFilterInstance(T filter) throws ServletException {
        getInjector().inject(filter);
        return filter;
    }

    public <T extends Servlet> T decorateServletInstance(T servlet) throws ServletException {
        getInjector().inject(servlet);
        return servlet;
    }

    public <T extends EventListener> T decorateListenerInstance(T listener) throws ServletException {
        getInjector().inject(listener);
        return listener;
    }

    public void decorateFilterHolder(FilterHolder filter) throws ServletException {
    }

    public void decorateServletHolder(ServletHolder servlet) throws ServletException {
    }

    public void destroyServletInstance(Servlet s) {
        getInjector().destroy(s);
    }

    public void destroyFilterInstance(Filter f) {
        getInjector().destroy(f);
    }

    public void destroyListenerInstance(EventListener f) {
        getInjector().destroy(f);
    }
}
