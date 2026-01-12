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
package org.jboss.weld.module.web.servlet;

import java.lang.annotation.Annotation;
import java.util.Collections;

import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Shutdown;
import jakarta.enterprise.event.Startup;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.BeanDeploymentModule;
import org.jboss.weld.bootstrap.BeanDeploymentModules;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.context.BoundContext;
import org.jboss.weld.context.ManagedContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.contexts.cache.RequestScopedCache;
import org.jboss.weld.event.EventMetadataImpl;
import org.jboss.weld.event.FastEvent;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.web.context.http.HttpRequestContextImpl;
import org.jboss.weld.module.web.context.http.HttpSessionDestructionContext;
import org.jboss.weld.module.web.logging.ServletLogger;
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

    public static final String ASYNC_STARTED_ATTR_NAME = "org.jboss.weld.context.asyncStarted";

    private static final String HTTP_SESSION = "org.jboss.weld." + HttpSession.class.getName();

    private static final String INCLUDE_HEADER = "jakarta.servlet.include.request_uri";
    private static final String FORWARD_HEADER = "jakarta.servlet.forward.request_uri";
    private static final String REQUEST_DESTROYED = HttpContextLifecycle.class.getName() + ".request.destroyed";

    private static final String GUARD_PARAMETER_NAME = "org.jboss.weld.context.ignore.guard.marker";
    private static final Object GUARD_PARAMETER_VALUE = new Object();

    private HttpSessionDestructionContext sessionDestructionContextCache;
    private HttpSessionContext sessionContextCache;
    private HttpRequestContext requestContextCache;

    private volatile Boolean conversationActivationEnabled;
    private final boolean ignoreForwards;
    private final boolean ignoreIncludes;

    private final BeanManagerImpl beanManager;
    private final ConversationContextActivator conversationContextActivator;
    private final HttpContextActivationFilter contextActivationFilter;

    private final FastEvent<HttpServletRequest> requestInitializedEvent;
    private final FastEvent<HttpServletRequest> requestBeforeDestroyedEvent;
    private final FastEvent<HttpServletRequest> requestDestroyedEvent;
    private final FastEvent<HttpSession> sessionInitializedEvent;
    private final FastEvent<HttpSession> sessionBeforeDestroyedEvent;
    private final FastEvent<HttpSession> sessionDestroyedEvent;

    private final ServletApiAbstraction servletApi;

    private final ServletContextService servletContextService;

    private final Container container;
    private final BeanDeploymentModule module;

    private static final ThreadLocal<Counter> nestedInvocationGuard = new ThreadLocal<HttpContextLifecycle.Counter>();
    private final boolean nestedInvocationGuardEnabled;

    private static class Counter {
        private int value = 1;
    }

    public HttpContextLifecycle(BeanManagerImpl beanManager, HttpContextActivationFilter contextActivationFilter,
            boolean ignoreForwards,
            boolean ignoreIncludes, boolean lazyConversationContext, boolean nestedInvocationGuardEnabled) {
        this.beanManager = beanManager;
        this.conversationContextActivator = new ConversationContextActivator(beanManager, lazyConversationContext);
        this.conversationActivationEnabled = null;
        this.ignoreForwards = ignoreForwards;
        this.ignoreIncludes = ignoreIncludes;
        this.contextActivationFilter = contextActivationFilter;
        this.requestInitializedEvent = FastEvent.of(HttpServletRequest.class, beanManager, Initialized.Literal.REQUEST);
        this.requestBeforeDestroyedEvent = FastEvent.of(HttpServletRequest.class, beanManager, BeforeDestroyed.Literal.REQUEST);
        this.requestDestroyedEvent = FastEvent.of(HttpServletRequest.class, beanManager, Destroyed.Literal.REQUEST);
        this.sessionInitializedEvent = FastEvent.of(HttpSession.class, beanManager, Initialized.Literal.SESSION);
        this.sessionBeforeDestroyedEvent = FastEvent.of(HttpSession.class, beanManager, BeforeDestroyed.Literal.SESSION);
        this.sessionDestroyedEvent = FastEvent.of(HttpSession.class, beanManager, Destroyed.Literal.SESSION);
        this.servletApi = beanManager.getServices().get(ServletApiAbstraction.class);
        this.servletContextService = beanManager.getServices().get(ServletContextService.class);
        this.nestedInvocationGuardEnabled = nestedInvocationGuardEnabled;
        this.container = Container.instance(beanManager);
        BeanDeploymentModules beanDeploymentModules = beanManager.getServices().get(BeanDeploymentModules.class);
        this.module = beanDeploymentModules != null ? beanDeploymentModules.getModule(beanManager) : null;
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
        servletContextService.contextInitialized(ctx);
        fireSynchronizedEvent(ctx, ServletContext.class, Initialized.Literal.APPLICATION);
        Environment env = Container.getEnvironment();
        if (module != null && env != null && env.automaticallyHandleStartupShutdownEvents()) {
            fireSynchronizedEvent(new Startup(), Startup.class, Any.Literal.INSTANCE);
        }
    }

    public void contextDestroyed(ServletContext ctx) {
        // firstly, fire Shutdown event
        Environment env = Container.getEnvironment();
        if (module != null && env != null && env.automaticallyHandleStartupShutdownEvents()) {
            fireSynchronizedEvent(new Shutdown(), Shutdown.class, Any.Literal.INSTANCE);
        }
        // TODO WELD-2282 Firing these two right after each other does not really make sense
        fireSynchronizedEvent(ctx, ServletContext.class, BeforeDestroyed.Literal.APPLICATION);
        fireSynchronizedEvent(ctx, ServletContext.class, Destroyed.Literal.APPLICATION);
    }

    private void fireSynchronizedEvent(Object payload, Class<?> payloadClass, Annotation qualifier) {
        if (module != null) {
            // Deliver events sequentially
            synchronized (container) {
                if (module.isWebModule()) {
                    module.fireEvent(payloadClass, payload, qualifier);
                } else {
                    // fallback for backward compatibility
                    ServletLogger.LOG.noEeModuleDescriptor(beanManager);
                    final EventMetadata metadata = new EventMetadataImpl(payloadClass, null,
                            Collections.singleton(qualifier));
                    beanManager.getAccessibleLenientObserverNotifier().fireEvent(payloadClass, payload, metadata,
                            qualifier);
                }
            }
        }
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
        RequestScopedCache.endRequest();
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
        if (nestedInvocationGuardEnabled) {
            Counter counter = nestedInvocationGuard.get();
            Object marker = request.getAttribute(GUARD_PARAMETER_NAME);
            if (counter != null && marker != null) {
                // this is a nested invocation, increment the counter and ignore this invocation
                counter.value++;
                return;
            } else {
                if (counter != null && marker == null) {
                    /*
                     * This request has not been processed yet but the guard is set already. That indicates, that the guard
                     * leaked from a previous request
                     * processing - most likely the Servlet container did not invoke listener methods symmetrically. Log a
                     * warning and recover by
                     * re-initializing the guard
                     */
                    ServletLogger.LOG.guardLeak(counter.value);
                }
                // this is the initial (outer) invocation
                nestedInvocationGuard.set(new Counter());
                request.setAttribute(GUARD_PARAMETER_NAME, GUARD_PARAMETER_VALUE);
            }
        }
        if (ignoreForwards && isForwardedRequest(request)) {
            return;
        }
        if (ignoreIncludes && isIncludedRequest(request)) {
            return;
        }
        if (!contextActivationFilter.accepts(request)) {
            return;
        }

        ServletLogger.LOG.requestInitialized(request);

        // cleanup any leftover destruction context on this thread, see WELD-2631
        if (getSessionDestructionContext().isActive()) {
            ServletLogger.LOG.destructionContextLeak();
            getSessionDestructionContext().deactivate();
        }

        SessionHolder.requestInitialized(request);

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
        if (isRequestDestroyed(request)) {
            return;
        }
        if (nestedInvocationGuardEnabled) {
            Counter counter = nestedInvocationGuard.get();
            if (counter != null) {
                counter.value--;
                if (counter.value > 0) {
                    return; // this is a nested invocation, ignore it
                } else {
                    nestedInvocationGuard.remove(); // this is the outer invocation
                    request.removeAttribute(GUARD_PARAMETER_NAME);
                }
            } else {
                ServletLogger.LOG.guardNotSet();
                return;
            }
        }
        if (ignoreForwards && isForwardedRequest(request)) {
            return;
        }
        if (ignoreIncludes && isIncludedRequest(request)) {
            return;
        }
        if (!contextActivationFilter.accepts(request)) {
            return;
        }

        ServletLogger.LOG.requestDestroyed(request);

        try {
            conversationContextActivator.deactivateConversationContext(request);
            /*
             * If this request has been switched to async then do not invalidate the context now as it will be invalidated at
             * the end of the async operation.
             */
            if (servletApi.isAsyncSupported() && servletApi.isAsyncStarted(request)) {
                // Note that we can't use isAsyncStarted() because it may return false after dispatch
                request.setAttribute(ASYNC_STARTED_ATTR_NAME, true);
            } else {
                getRequestContext().invalidate();
            }

            // fire @BeforeDestroyed(RequestScoped.class)
            requestBeforeDestroyedEvent.fire(request);
            safelyDeactivate(getRequestContext(), request);
            // fire @Destroyed(RequestScoped.class)
            requestDestroyedEvent.fire(request);

            Object destroyedHttpSession = request.getAttribute(HTTP_SESSION);
            // fire @BeforeDestroyed(SessionScoped.class)
            if (destroyedHttpSession != null) {
                sessionBeforeDestroyedEvent.fire((HttpSession) destroyedHttpSession);
            }
            safelyDeactivate(getSessionContext(), request);
            // fire @Destroyed(SessionScoped.class)
            if (destroyedHttpSession != null) {
                sessionDestroyedEvent.fire((HttpSession) destroyedHttpSession);
            }
        } finally {
            safelyDissociate(getRequestContext(), request);
            // WFLY-1533 Underlying HTTP session may be invalid
            safelyDissociate(getSessionContext(), request);

            // Catch block is inside the activator method so that we're able to log the context
            conversationContextActivator.disassociateConversationContext(request);

            SessionHolder.clear();
        }
    }

    public boolean isConversationActivationSet() {
        return conversationActivationEnabled != null;
    }

    public void setConversationActivationEnabled(boolean conversationActivationEnabled) {
        this.conversationActivationEnabled = conversationActivationEnabled;
    }

    @Override
    public void cleanup() {
    }

    /**
     * Some Servlet containers fire HttpServletListeners for include requests (inner requests caused by calling the include
     * method of RequestDispatcher). This
     * causes problems with context shut down as context manipulation is not reentrant. This method detects if this request is
     * an included request or not.
     */
    private boolean isIncludedRequest(HttpServletRequest request) {
        return request.getAttribute(INCLUDE_HEADER) != null;
    }

    /**
     * Some Servlet containers fire HttpServletListeners for forward requests (inner requests caused by calling the forward
     * method of RequestDispatcher). This
     * causes problems with context shut down as context manipulation is not reentrant. This method detects if this request is
     * an forwarded request or not.
     */
    private boolean isForwardedRequest(HttpServletRequest request) {
        return request.getAttribute(FORWARD_HEADER) != null;
    }

    /**
     * The way servlet containers react to an exception that occurs in a {@link ServletRequestListener} differs among servlet
     * listeners. In certain containers
     * the destroyed callback may be invoked multiple times, causing the latter invocations to fail as thread locals have
     * already been unset. We use the
     * {@link #REQUEST_DESTROYED} flag to indicate that all further invocations of the
     * {@link ServletRequestListener#requestDestroyed(jakarta.servlet.ServletRequestEvent)} should be ignored by Weld.
     */
    private boolean isRequestDestroyed(HttpServletRequest request) {
        return request.getAttribute(REQUEST_DESTROYED) != null;
    }

    private <T> void safelyDissociate(BoundContext<T> context, T storage) {
        try {
            context.dissociate(storage);
        } catch (Exception e) {
            ServletLogger.LOG.unableToDissociateContext(context, storage);
            ServletLogger.LOG.catchingDebug(e);
        }
    }

    private void safelyDeactivate(ManagedContext context, HttpServletRequest request) {
        try {
            context.deactivate();
        } catch (Exception e) {
            ServletLogger.LOG.unableToDeactivateContext(context, request);
            ServletLogger.LOG.catchingDebug(e);
        }
    }

}
