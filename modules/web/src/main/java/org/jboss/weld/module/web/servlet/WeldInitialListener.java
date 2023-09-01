/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.web.servlet;

import static org.jboss.weld.module.web.servlet.ConversationFilter.CONVERSATION_FILTER_REGISTERED;
import static org.jboss.weld.servlet.api.InitParameters.CONVERSATION_CONTEXT_LAZY_PARAM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSessionEvent;

import org.jboss.weld.Container;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.module.web.logging.ServletLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.BeanManagers;
import org.jboss.weld.servlet.api.InitParameters;
import org.jboss.weld.servlet.api.helpers.AbstractServletListener;
import org.jboss.weld.servlet.spi.HttpContextActivationFilter;
import org.jboss.weld.module.web.util.servlet.ServletUtils;

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

    private static final String CONTEXT_IGNORE_GUARD_PARAMETER = "org.jboss.weld.context.ignore.guard";

    @Inject
    private BeanManagerImpl beanManager;
    private HttpContextLifecycle lifecycle;

    public WeldInitialListener() {
    }

    public WeldInitialListener(BeanManagerImpl beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final ServletContext ctx = sce.getServletContext();
        // First try to use the context id obtained from the servlet context (OSGi, Servlet containers, etc.)
        if (beanManager == null) {
            String contextId = ctx.getInitParameter(Container.CONTEXT_ID_KEY);
            if (contextId != null) {
                List<BeanManagerImpl> managers = new ArrayList<BeanManagerImpl>(Container.instance(contextId).beanDeploymentArchives().values());
                Collections.sort(managers, BeanManagers.ID_COMPARATOR);
                beanManager = managers.get(0);
            }
        }
        // servlet containers may not be able to inject fields in a servlet listener
        if (beanManager == null) {
            beanManager = BeanManagerProxy.unwrap(CDI.current().getBeanManager());
        }
        HttpContextActivationFilter filter = ServletUtils.getContextActivationFilter(beanManager, ctx);
        final boolean ignoreForwards = getBooleanInitParameter(ctx, InitParameters.CONTEXT_IGNORE_FORWARD, false);
        final boolean ignoreIncludes = getBooleanInitParameter(ctx, InitParameters.CONTEXT_IGNORE_INCLUDE, false);
        final boolean nestedInvocationGuard = getBooleanInitParameter(ctx, CONTEXT_IGNORE_GUARD_PARAMETER, true);
        final boolean lazyConversationContext = getBooleanInitParameter(ctx, CONVERSATION_CONTEXT_LAZY_PARAM, true);
        this.lifecycle = new HttpContextLifecycle(beanManager, filter, ignoreForwards, ignoreIncludes, lazyConversationContext, nestedInvocationGuard);
        if (Boolean.valueOf(ctx.getInitParameter(CONVERSATION_FILTER_REGISTERED))) {
            this.lifecycle.setConversationActivationEnabled(false);
        }
        this.lifecycle.contextInitialized(ctx);
        ctx.setAttribute(WeldInitialListener.class.getName(), this);
    }

    private boolean getBooleanInitParameter(ServletContext ctx, String parameterName, boolean defaultValue) {
        String value = ctx.getInitParameter(parameterName);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.valueOf(value);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // null means an error while starting the app; for instance and exception in ServletContainerInitializer#onStartup
        if (this.lifecycle != null) {
            lifecycle.contextDestroyed(sce.getServletContext());
        }
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
