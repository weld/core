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

            if (jwi == null) {
                throw new IllegalArgumentException("No such Jetty injector found in servlet context attributes.");
            }

            injector = jwi;
        }

        return injector;
    }

    /**
     * Eclipse Jetty 7/8/9.0 - decorate
     */
    public <T extends Filter> T decorateFilterInstance(T filter) throws ServletException {
        getInjector().inject(filter);
        return filter;
    }

    /**
     * Eclipse Jetty 7/8/9.0 - decorate
     */
    public <T extends Servlet> T decorateServletInstance(T servlet) throws ServletException {
        getInjector().inject(servlet);
        return servlet;
    }

    /**
     * Eclipse Jetty 7/8/9.0 - decorate
     */
    public <T extends EventListener> T decorateListenerInstance(T listener) throws ServletException {
        getInjector().inject(listener);
        return listener;
    }

    /**
     * Eclipse Jetty 7/8/9.0 - decorate
     */
    public void decorateFilterHolder(FilterHolder filter) throws ServletException {
    }

    /**
     * Eclipse Jetty 7/8/9.0 - decorate
     */
    public void decorateServletHolder(ServletHolder servlet) throws ServletException {
    }

    /**
     * Eclipse Jetty 7/8/9.0 - destroy
     */
    public void destroyServletInstance(Servlet s) {
        getInjector().destroy(s);
    }

    /**
     * Eclipse Jetty 7/8/9.0 - destroy
     */
    public void destroyFilterInstance(Filter f) {
        getInjector().destroy(f);
    }

    /**
     * Eclipse Jetty 7/8/9.0 - destroy
     */
    public void destroyListenerInstance(EventListener f) {
        getInjector().destroy(f);
    }

    /**
     * Eclipse Jetty 9.1 - decorate
     */
    public Object decorate(Object o) {
        getInjector().inject(o);
        return o;
    }

    /**
     * Eclipse Jetty 9.1 - destroy
     */
    public void destroy(Object o) {
        getInjector().destroy(o);
    }
}
