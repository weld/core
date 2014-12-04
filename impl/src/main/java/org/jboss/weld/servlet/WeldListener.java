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
import static org.jboss.weld.logging.messages.ServletMessage.UNABLE_TO_DEACTIVATE_CONTEXT;
import static org.jboss.weld.logging.messages.ServletMessage.UNABLE_TO_DISSOCIATE_CONTEXT;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.weld.Container;
import org.jboss.weld.context.BoundContext;
import org.jboss.weld.context.ManagedContext;
import org.jboss.weld.context.cache.RequestScopedBeanCache;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.messages.ServletMessage;
import org.jboss.weld.servlet.api.helpers.AbstractServletListener;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLogger.Level;

/**
 * The Weld listener
 * <p/>
 * Listens for context/session creation/destruction.
 * <p/>
 * Delegates work to the ServletLifeCycle.
 *
 * @author Nicklas Karlsson
 */
public class WeldListener extends AbstractServletListener {

    private static final LocLogger log = loggerFactory().getLogger(SERVLET);
    private static final XLogger xLog = loggerFactory().getXLogger(SERVLET);

    public static final String CONTEXT_IGNORE_FORWARD = "org.jboss.weld.context.ignore.forward";
    public static final String CONTEXT_IGNORE_INCLUDE = "org.jboss.weld.context.ignore.include";
    public static final String CONTEXT_IGNORE_GUARD = "org.jboss.weld.context.ignore.guard";

    private static final String INCLUDE_HEADER = "javax.servlet.include.request_uri";
    private static final String FORWARD_HEADER = "javax.servlet.forward.request_uri";
    private static final String GUARD_PARAMETER_NAME = "org.jboss.weld.context.ignore.guard.marker";
    private static final Object GUARD_PARAMETER_VALUE = new Object();

    private boolean ignoreForwards;
    private boolean ignoreIncludes;
    private boolean nestedInvocationGuardEnabled;

    private transient HttpSessionContext sessionContextCache;
    private transient HttpRequestContext requestContextCache;
    private transient HttpConversationContext conversationContextCache;

    private static final ThreadLocal<Counter> nestedInvocationGuard = new ThreadLocal<Counter>();

    private static class Counter {
        private int value = 1;
    }

    private HttpSessionContext sessionContext() {
        if (sessionContextCache == null) {
            this.sessionContextCache = Container.instance().deploymentManager().instance().select(HttpSessionContext.class).get();
        }
        return sessionContextCache;
    }

    private HttpRequestContext requestContext() {
        if (requestContextCache == null) {
            this.requestContextCache = Container.instance().deploymentManager().instance().select(HttpRequestContext.class).get();
        }
        return requestContextCache;
    }

    private HttpConversationContext conversationContext() {
        if (conversationContextCache == null) {
            this.conversationContextCache = Container.instance().deploymentManager().instance().select(HttpConversationContext.class).get();
        }
        return conversationContextCache;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        // JBoss AS will still start the deployment even if WB fails
        if (Container.available()) {
            // Mark the session context and conversation contexts to destroy
            // instances when appropriate
            sessionContext().destroy(event.getSession());
            RequestScopedBeanCache.endRequest();
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        if (nestedInvocationGuardEnabled) {
            Counter counter = nestedInvocationGuard.get();
            if (counter != null) {
                counter.value--;
                if (counter.value > 0) {
                    return; // this is a nested invocation, ignore it
                } else {
                    nestedInvocationGuard.remove(); // this is the outer invocation
                    event.getServletRequest().removeAttribute(GUARD_PARAMETER_NAME);
                }
            } else {
                log.warn(ServletMessage.GUARD_NOT_SET);
                return;
            }
        }
        if (ignoreForwards && isForwardedRequest(event.getServletRequest())) {
            return;
        }
        if (ignoreIncludes && isIncludedRequest(event.getServletRequest())) {
            return;
        }
        log.trace(REQUEST_DESTROYED, event.getServletRequest());
        // JBoss AS will still start the deployment even if WB fails
        if (Container.available()) {
            if (event.getServletRequest() instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) event.getServletRequest();

                try {
                    requestContext().invalidate();
                    safelyDeactivate(requestContext(), request);
                    safelyDeactivate(sessionContext(), request);
                    /*
                    * The conversation context is invalidated and deactivated in the
                    * WeldPhaseListener, however if an exception is thrown by the action
                    * method, we can't detect that in the phase listener. Make sure it
                    * happens!
                    */
                    if (conversationContext().isActive()) {
                        safelyDeactivate(conversationContext(), request);
                    }
                } finally {
                    safelyDissociate(requestContext(), request);
                    safelyDissociate(sessionContext(), request);
                    safelyDissociate(conversationContext(), request);
                }
            } else {
                throw new IllegalStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
            }
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        if (nestedInvocationGuardEnabled) {
            Counter counter = nestedInvocationGuard.get();
            Object marker = event.getServletRequest().getAttribute(GUARD_PARAMETER_NAME);
            if (counter != null && marker != null) {
                // this is a nested invocation, increment the counter and ignore this invocation
                counter.value++;
                return;
            } else {
                if (counter != null && marker == null) {
                    /*
                     * This request has not been processed yet but the guard is set already.
                     * That indicates, that the guard leaked from a previous request
                     * processing. Log a warning and recover by re-initializing the guard
                     */
                    log.warn(ServletMessage.GUARD_LEAKED, counter.value);
                }
                // this is the initial (outer) invocation
                nestedInvocationGuard.set(new Counter());
                event.getServletRequest().setAttribute(GUARD_PARAMETER_NAME, GUARD_PARAMETER_VALUE);
            }
        }
        if (ignoreForwards && isForwardedRequest(event.getServletRequest())) {
            return;
        }
        if (ignoreIncludes && isIncludedRequest(event.getServletRequest())) {
            return;
        }
        log.trace(REQUEST_INITIALIZED, event.getServletRequest());
        // JBoss AS will still start the deployment even if Weld fails to start
        if (Container.available()) {
            if (event.getServletRequest() instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) event.getServletRequest();

                requestContext().associate(request);
                sessionContext().associate(request);
                conversationContext().associate(request);
                /*
                * The conversation context is activated in the WeldPhaseListener
                */

                requestContext().activate();
                sessionContext().activate();
            } else {
                throw new IllegalStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
            }

        }
    }

    /**
     * Some Servlet containers fire HttpServletListeners for include requests (inner requests caused by calling the include method of RequestDispatcher). This
     * causes problems with context shut down as context manipulation is not reentrant. This method detects if this request is an included request or not.
     */
    private boolean isIncludedRequest(ServletRequest request) {
        return request.getAttribute(INCLUDE_HEADER) != null;
    }

    /**
     * Some Servlet containers fire HttpServletListeners for forward requests (inner requests caused by calling the forward method of RequestDispatcher). This
     * causes problems with context shut down as context manipulation is not reentrant. This method detects if this request is an forwarded request or not.
     */
    private boolean isForwardedRequest(ServletRequest request) {
        return request.getAttribute(FORWARD_HEADER) != null;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.ignoreForwards = getBooleanInitParameter(sce.getServletContext(), CONTEXT_IGNORE_FORWARD, false);
        this.ignoreIncludes = getBooleanInitParameter(sce.getServletContext(), CONTEXT_IGNORE_INCLUDE, false);
        this.nestedInvocationGuardEnabled = getBooleanInitParameter(sce.getServletContext(), CONTEXT_IGNORE_GUARD, true);
    }

    private boolean getBooleanInitParameter(ServletContext ctx, String parameterName, boolean defaultValue) {
        String value = ctx.getInitParameter(parameterName);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.valueOf(value);
    }

    private <T> void safelyDissociate(BoundContext<T> context, T storage) {
        try {
            context.dissociate(storage);
        } catch (Exception e) {
            log.warn(UNABLE_TO_DISSOCIATE_CONTEXT, context, storage);
            xLog.throwing(Level.DEBUG, e);
        }
    }

    private void safelyDeactivate(ManagedContext context, HttpServletRequest request) {
        try {
            context.deactivate();
        } catch (Exception e) {
            log.warn(UNABLE_TO_DEACTIVATE_CONTEXT, context, request);
            xLog.throwing(Level.DEBUG, e);
        }
    }
}
