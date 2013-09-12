/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.servlet;

import static org.jboss.weld.servlet.ConversationFilter.CONVERSATION_FILTER_REGISTERED;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.weld.Container;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.logging.ServletLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.servlet.api.helpers.AbstractServletListener;
import org.jboss.weld.servlet.spi.HttpContextActivationFilter;
import org.jboss.weld.util.servlet.ServletUtils;

/**
 * The initial Weld listener. It should always be registered as the first listener, before any
 * other (application) listeners.
 * <p/>
 * Listens for context/session/request creation/destruction.
 * <p/>
 * Delegates work to the HttpContextLifecycle.
 *
 * @author Nicklas Karlsson
 * @author Dan Allen
 * @author Ales Justin
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Jozef Hartinger
 * @author Marko Luksa
 */
public class WeldInitialListener extends AbstractServletListener {

    @Inject
    private BeanManagerImpl beanManager;
    private HttpContextLifecycle lifecycle;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // if running in OSGi environment, use the context id obtained from servlet context
        if (beanManager == null) {
            String contextId = sce.getServletContext().getInitParameter(Container.CONTEXT_ID_KEY);
            if (contextId != null) {
                Container instance = Container.instance(contextId);
                if (instance.beanDeploymentArchives().size() == 1) {
                    this.beanManager = instance.beanDeploymentArchives().values().iterator().next();
                }
            }
        }
        // servlet containers may not be able to inject fields in a servlet listener
        if (beanManager == null) {
            beanManager = BeanManagerProxy.unwrap(CDI.current().getBeanManager());
        }
        HttpContextActivationFilter filter = ServletUtils.getContextActivationFilter(beanManager, sce.getServletContext());
        this.lifecycle = new HttpContextLifecycle(beanManager, filter);
        if (Boolean.valueOf(sce.getServletContext().getInitParameter(CONVERSATION_FILTER_REGISTERED))) {
            this.lifecycle.setConversationActivationEnabled(false);
        }
        this.lifecycle.contextInitialized(sce.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        lifecycle.contextDestroyed(sce.getServletContext());
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        lifecycle.sessionCreated(event.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        lifecycle.sessionDestroyed(event.getSession());
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        if (event.getServletRequest() instanceof HttpServletRequest) {
            lifecycle.requestDestroyed((HttpServletRequest) event.getServletRequest());
        } else {
            throw ServletLogger.LOG.onlyHttpServletLifecycleDefined();
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        if (!lifecycle.isConversationActivationSet()) {
            Object value = event.getServletContext().getAttribute(CONVERSATION_FILTER_REGISTERED);
            if (Boolean.TRUE.equals(value)) {
                this.lifecycle.setConversationActivationEnabled(false);
            } else {
                this.lifecycle.setConversationActivationEnabled(true);
            }
        }
        if (event.getServletRequest() instanceof HttpServletRequest) {
            lifecycle.requestInitialized((HttpServletRequest) event.getServletRequest(), event.getServletContext());
        } else {
            throw ServletLogger.LOG.onlyHttpServletLifecycleDefined();
        }
    }
}
