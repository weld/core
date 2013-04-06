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

import static org.jboss.weld.logging.Category.SERVLET;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ServletMessage.ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED;
import static org.jboss.weld.logging.messages.ServletMessage.REQUEST_DESTROYED;
import static org.jboss.weld.logging.messages.ServletMessage.REQUEST_INITIALIZED;
import static org.jboss.weld.servlet.ConversationFilter.CONVERSATION_FILTER_REGISTERED;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bean.builtin.ee.ServletContextBean;
import org.jboss.weld.context.cache.RequestScopedBeanCache;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpRequestContextImpl;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.literal.DestroyedLiteral;
import org.jboss.weld.literal.InitializedLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.servlet.api.helpers.AbstractServletListener;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;

/**
 * The Weld listener
 * <p/>
 * Listens for context/session creation/destruction.
 * <p/>
 * Delegates work to the ServletLifeCycle.
 *
 * @author Nicklas Karlsson
 * @author Dan Allen
 * @author Ales Justin
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Jozef Hartinger
 * @author Marko Luksa
 */
public class WeldListener extends AbstractServletListener {

    private static final String HTTP_SESSION = "org.jboss.weld." + HttpSession.class.getName();

    private static final LocLogger log = loggerFactory().getLogger(SERVLET);

    private HttpSessionContext sessionContextCache;
    private HttpRequestContext requestContextCache;

    private ConversationContextActivator conversationContextActivator;

    private volatile Boolean conversationFilterRegistered;

    @Inject
    private BeanManagerImpl beanManager;

    private HttpSessionContext sessionContext() {
        if (sessionContextCache == null) {
            this.sessionContextCache = beanManager.instance().select(HttpSessionContext.class).get();
        }
        return sessionContextCache;
    }

    private HttpRequestContext requestContext() {
        if (requestContextCache == null) {
            this.requestContextCache = beanManager.instance().select(HttpRequestContext.class).get();
        }
        return requestContextCache;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String initParam = sce.getServletContext().getInitParameter(CONVERSATION_FILTER_REGISTERED);
        if (initParam != null) {
            this.conversationFilterRegistered = Boolean.valueOf(initParam);
        }
        if (beanManager == null) {
            // servlet containers may not be able to inject fields in a servlet listener
            beanManager = BeanManagerProxy.unwrap(CDI.current().getBeanManager());
        }
        beanManager.getAccessibleLenientObserverNotifier().fireEvent(sce.getServletContext(), InitializedLiteral.APPLICATION);
        this.conversationContextActivator = new ConversationContextActivator(beanManager, sce.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        beanManager.getAccessibleLenientObserverNotifier().fireEvent(sce.getServletContext(), DestroyedLiteral.APPLICATION);
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        SessionHolder.sessionCreated(event);
        beanManager.getAccessibleLenientObserverNotifier().fireEvent(event.getSession(), InitializedLiteral.SESSION);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        // Mark the session context and conversation contexts to destroy
        // instances when appropriate
        HttpSession session = event.getSession();
        boolean destroyed = sessionContext().destroy(session);
        SessionHolder.clear();
        RequestScopedBeanCache.endRequest();
        if (destroyed) {
            // we are outside of a request (the session timed out) and therefore the session was destroyed immediately
            // we can fire the @Destroyed(SessionScoped.class) event immediately
            beanManager.getAccessibleLenientObserverNotifier().fireEvent(session, DestroyedLiteral.SESSION);
        } else {
            // the old session won't be available at the time we destroy this request
            // let's store its reference until then
            if (requestContext() instanceof HttpRequestContextImpl) {
                HttpServletRequest request = Reflections.<HttpRequestContextImpl> cast(requestContext()).getHttpServletRequest();
                request.setAttribute(HTTP_SESSION, session);
            }
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        log.trace(REQUEST_DESTROYED, event.getServletRequest());
        if (event.getServletRequest() instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) event.getServletRequest();

            try {
                conversationContextActivator.deactivateConversationContext(request);
                requestContext().invalidate();
                requestContext().deactivate();
                // fire @Destroyed(RequestScoped.class)
                beanManager.getAccessibleLenientObserverNotifier().fireEvent(request, DestroyedLiteral.REQUEST);
                sessionContext().deactivate();
                // fire @Destroyed(SessionScoped.class)
                if (!sessionContext().isValid()) {
                    beanManager.getAccessibleLenientObserverNotifier().fireEvent(request.getAttribute(HTTP_SESSION), DestroyedLiteral.SESSION);
                }
            } finally {
                requestContext().dissociate(request);
                sessionContext().dissociate(request);
                conversationContextActivator.disassociateConversationContext(request);
                SessionHolder.clear();
                ServletContextBean.cleanup();
            }
        } else {
            throw new IllegalStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        log.trace(REQUEST_INITIALIZED, event.getServletRequest());

        if (conversationFilterRegistered == null) {
            Object value = event.getServletContext().getAttribute(CONVERSATION_FILTER_REGISTERED);
            conversationFilterRegistered = Boolean.TRUE.equals(value);
        }

        if (event.getServletRequest() instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) event.getServletRequest();

            SessionHolder.requestInitialized(request);

            ServletContextBean.setServletContext(event.getServletContext());

            requestContext().associate(request);
            sessionContext().associate(request);
            if (!conversationFilterRegistered) {
                conversationContextActivator.associateConversationContext(request);
            }

            requestContext().activate();
            sessionContext().activate();

            try {
                if (!conversationFilterRegistered) {
                    conversationContextActivator.activateConversationContext(request);
                }
                beanManager.getAccessibleLenientObserverNotifier().fireEvent(request, InitializedLiteral.REQUEST);
            } catch (RuntimeException e) {
                requestDestroyed(event);
                throw e;
            }
        } else {
            throw new IllegalStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
        }
    }
}
