/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.bean.builtin.ee.ServletContextBean;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.context.cache.RequestScopedBeanCache;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpRequestContextImpl;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.context.http.HttpSessionDestructionContext;
import org.jboss.weld.event.FastEvent;
import org.jboss.weld.literal.DestroyedLiteral;
import org.jboss.weld.literal.InitializedLiteral;
import org.jboss.weld.logging.ServletLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.servlet.spi.HttpContextActivationFilter;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Takes care of setting up and tearing down CDI contexts around an HTTP request and dispatching context lifecycle events.
 *
 * @author Jozef Hartinger
 * @author Marko Luksa
 *
 */
public class HttpContextLifecycle implements Service {

    private static final String HTTP_SESSION = "org.jboss.weld." + HttpSession.class.getName();

    private static final String INCLUDE_HEADER = "javax.servlet.include.request_uri";
    private static final String REQUEST_DESTROYED = HttpContextLifecycle.class.getName() + ".request.destroyed";

    private HttpSessionDestructionContext sessionDestructionContextCache;
    private HttpSessionContext sessionContextCache;
    private HttpRequestContext requestContextCache;

    private volatile Boolean conversationActivationEnabled;

    private final BeanManagerImpl beanManager;
    private final ConversationContextActivator conversationContextActivator;
    private final HttpContextActivationFilter contextActivationFilter;

    private final FastEvent<ServletContext> applicationInitializedEvent;
    private final FastEvent<ServletContext> applicationDestroyedEvent;
    private final FastEvent<HttpServletRequest> requestInitializedEvent;
    private final FastEvent<HttpServletRequest> requestDestroyedEvent;
    private final FastEvent<HttpSession> sessionInitializedEvent;
    private final FastEvent<HttpSession> sessionDestroyedEvent;

    private final ServletApiAbstraction servletApi;

    public HttpContextLifecycle(BeanManagerImpl beanManager, HttpContextActivationFilter contextActivationFilter) {
        this.beanManager = beanManager;
        this.conversationContextActivator = new ConversationContextActivator(beanManager);
        this.conversationActivationEnabled = null;
        this.contextActivationFilter = contextActivationFilter;
        this.applicationInitializedEvent = FastEvent.of(ServletContext.class, beanManager, InitializedLiteral.APPLICATION);
        this.applicationDestroyedEvent = FastEvent.of(ServletContext.class, beanManager, DestroyedLiteral.APPLICATION);
        this.requestInitializedEvent = FastEvent.of(HttpServletRequest.class, beanManager, InitializedLiteral.REQUEST);
        this.requestDestroyedEvent = FastEvent.of(HttpServletRequest.class, beanManager, DestroyedLiteral.REQUEST);
        this.sessionInitializedEvent = FastEvent.of(HttpSession.class, beanManager, InitializedLiteral.SESSION);
        this.sessionDestroyedEvent = FastEvent.of(HttpSession.class, beanManager, DestroyedLiteral.SESSION);
        this.servletApi = beanManager.getServices().get(ServletApiAbstraction.class);
    }

    private HttpSessionDestructionContext getSessionDestructionContext() {
        if (sessionDestructionContextCache == null) {
            this.sessionDestructionContextCache = beanManager.instance().select(HttpSessionDestructionContext.class).get();
        }
        return sessionDestructionContextCache;
    }

    private HttpSessionContext getSessionContext() {
        if (sessionContextCache == null) {
            this.sessionContextCache = beanManager.instance().select(HttpSessionContext.class).get();
        }
        return sessionContextCache;
    }

    public HttpRequestContext getRequestContext() {
        if (requestContextCache == null) {
            this.requestContextCache = beanManager.instance().select(HttpRequestContext.class).get();
        }
        return requestContextCache;
    }

    public void contextInitialized(ServletContext ctx) {
        applicationInitializedEvent.fire(ctx);
    }

    public void contextDestroyed(ServletContext ctx) {
        applicationDestroyedEvent.fire(ctx);
    }

    public void sessionCreated(HttpSession session) {
        SessionHolder.sessionCreated(session);
        conversationContextActivator.sessionCreated(session);
        sessionInitializedEvent.fire(session);
    }

    public void sessionDestroyed(HttpSession session) {
        // Mark the session context and conversation contexts to destroy
        // instances when appropriate
        deactivateSessionDestructionContext(session);
        boolean destroyed = getSessionContext().destroy(session);
        SessionHolder.clear();
        RequestScopedBeanCache.endRequest();
        if (destroyed) {
            // we are outside of a request (the session timed out) and therefore the session was destroyed immediately
            // we can fire the @Destroyed(SessionScoped.class) event immediately
            sessionDestroyedEvent.fire(session);
        } else {
            // the old session won't be available at the time we destroy this request
            // let's store its reference until then
            if (getRequestContext() instanceof HttpRequestContextImpl) {
                HttpServletRequest request = Reflections.<HttpRequestContextImpl> cast(getRequestContext())
                        .getHttpServletRequest();
                request.setAttribute(HTTP_SESSION, session);
            }
        }
    }

    private void deactivateSessionDestructionContext(HttpSession session) {
        HttpSessionDestructionContext context = getSessionDestructionContext();
        if (context.isActive()) {
            context.deactivate();
            context.dissociate(session);
        }
    }

    public void requestInitialized(HttpServletRequest request, ServletContext ctx) {
        if (isIncludedRequest(request)) {
            return;
        }
        if (!contextActivationFilter.accepts(request)) {
            return;
        }

        ServletLogger.LOG.requestInitialized(request);

        SessionHolder.requestInitialized(request);

        ServletContextBean.setServletContext(ctx);

        getRequestContext().associate(request);
        getSessionContext().associate(request);
        if (conversationActivationEnabled) {
            conversationContextActivator.associateConversationContext(request);
        }

        getRequestContext().activate();
        getSessionContext().activate();

        try {
            if (conversationActivationEnabled) {
                conversationContextActivator.activateConversationContext(request);
            }
            requestInitializedEvent.fire(request);
        } catch (RuntimeException e) {
            try {
                requestDestroyed(request);
            } catch (Exception ignored) {
                // ignored in order to let the original exception be thrown
            }
            /*
             * If the servlet container happens to call the destroyed callback again, ignore it.
             */
            request.setAttribute(REQUEST_DESTROYED, Boolean.TRUE);
            throw e;
        }
    }

    public void requestDestroyed(HttpServletRequest request) {
        if (isIncludedRequest(request) || isRequestDestroyed(request)) {
            return;
        }
        if (!contextActivationFilter.accepts(request)) {
            return;
        }

        ServletLogger.LOG.requestDestroyed(request);

        try {
            conversationContextActivator.deactivateConversationContext(request);
            /*
             * if this request has been switched to async then do not invalidate the context now
             * as it will be invalidated at the end of the async operation.
             */
            if (!servletApi.isAsyncSupported() || !request.isAsyncStarted()) {
                getRequestContext().invalidate();
            }
            getRequestContext().deactivate();
            // fire @Destroyed(RequestScoped.class)
            requestDestroyedEvent.fire(request);
            getSessionContext().deactivate();
            // fire @Destroyed(SessionScoped.class)
            if (!getSessionContext().isValid()) {
                sessionDestroyedEvent.fire((HttpSession) request.getAttribute(HTTP_SESSION));
            }
        } finally {
            getRequestContext().dissociate(request);
            getSessionContext().dissociate(request);
            conversationContextActivator.disassociateConversationContext(request);
            SessionHolder.clear();
            ServletContextBean.cleanup();
        }
    }

    public boolean isConversationActivationSet() {
        return conversationActivationEnabled != null;
    }

    public void setConversationActivationEnabled(boolean conversationActivationEnabled) {
        this.conversationActivationEnabled = conversationActivationEnabled;
    }

    /**
     * Some Servlet containers fire HttpServletListeners for include requests (inner requests caused by calling the include method of RequestDispatcher). This
     * causes problems with context shut down as context manipulation is not reentrant. This method detects if this request is an included request or not.
     */
    private boolean isIncludedRequest(HttpServletRequest request) {
        return request.getAttribute(INCLUDE_HEADER) != null;
    }

    /**
     * The way servlet containers react to an exception that occurs in a {@link ServletRequestListener} differs among servlet listeners. In certain containers
     * the destroyed callback may be invoked multiple times, causing the latter invocations to fail as thread locals have already been unset. We use the
     * {@link #REQUEST_DESTROYED} flag to indicate that all further invocations of the
     * {@link ServletRequestListener#requestDestroyed(javax.servlet.ServletRequestEvent)} should be ignored by Weld.
     */
    private boolean isRequestDestroyed(HttpServletRequest request) {
        return request.getAttribute(REQUEST_DESTROYED) != null;
    }

    @Override
    public void cleanup() {
    }
}
