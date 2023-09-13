/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.servlet;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.http.HttpSessionEvent;

import org.jboss.weld.environment.ContainerInstance;
import org.jboss.weld.environment.ContainerInstanceFactory;
import org.jboss.weld.environment.servlet.logging.WeldServletLogger;
import org.jboss.weld.servlet.api.ServletListener;
import org.jboss.weld.servlet.api.helpers.ForwardingServletListener;
import org.jboss.weld.util.Preconditions;

/**
 * This is the original listener which had to be defined in web.xml.
 *
 * It's not necessary to register this listener in Servlet 3.0 compliant containers unless there are listener ordering
 * conflicts. E.g. if a user provides a
 * custom listener the request context will not be active during its notifications. In this case place this listener before any
 * other listener definitions in
 * web.xml.
 *
 * {@link ServletContextListener#contextInitialized(ServletContextEvent)} is no-op in case of the {@link EnhancedListener} is
 * registered as well.
 *
 * @author Pete Muir
 * @author Ales Justin
 * @see EnhancedListener
 */
public class Listener extends ForwardingServletListener {

    public static final String CONTAINER_ATTRIBUTE_NAME = WeldServletLifecycle.class.getPackage().getName() + ".container";
    static final String LISTENER_USED_ATTRIBUTE_NAME = EnhancedListener.class.getPackage().getName() + ".listenerUsed";

    /**
     * Creates a new Listener that uses the given {@link BeanManager} instead of initializing a new Weld container instance.
     *
     * @param manager the bean manager to be used
     * @return a new Listener instance
     */
    public static Listener using(BeanManager manager) {
        return new Listener(Collections.singletonList(initAction(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, manager)));
    }

    /**
     * Creates a new Listener that uses the given {@link ContainerInstance} (e.g.
     * {@link org.jboss.weld.environment.se.WeldContainer}) instead of initializing a
     * new Weld container instance. The listener does not take over the responsibility for container instance lifecycle
     * management. It is the caller's
     * responsibility to shut down the container instance properly. The listener will not shut down the container instance when
     * the Servlet context is
     * destroyed.
     *
     * @param container the container instance to be used
     * @return a new Listener instance
     */
    public static Listener using(ContainerInstance container) {
        return new Listener(Collections.singletonList(initAction(CONTAINER_ATTRIBUTE_NAME, container)));
    }

    /**
     * Creates a new Listener that uses the given {@link ContainerInstanceFactory} for initializing Weld instance. A new Weld
     * instance will be initialized using
     * {@link ContainerInstanceFactory#initialize()} when the Servlet context is initialized. The Weld instance will be shut
     * down when Servlet context is
     * destroyed.
     *
     * @param container the container factory to be used
     * @return a new Listener instance
     */
    public static Listener using(ContainerInstanceFactory container) {
        return new Listener(Collections.singletonList(initAction(CONTAINER_ATTRIBUTE_NAME, container)));
    }

    private static Consumer<ServletContext> initAction(String key, Object value) {
        Preconditions.checkNotNull(value);
        return (context -> context.setAttribute(key, value));
    }

    private volatile WeldServletLifecycle lifecycle;
    private final List<Consumer<ServletContext>> initActions;

    public Listener() {
        this.initActions = Collections.emptyList();
    }

    private Listener(List<Consumer<ServletContext>> initActions) {
        this.initActions = initActions;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        lifecycle = (WeldServletLifecycle) context.getAttribute(WeldServletLifecycle.INSTANCE_ATTRIBUTE_NAME);
        context.setAttribute(LISTENER_USED_ATTRIBUTE_NAME, Boolean.TRUE);
        if (Boolean.TRUE.equals(context.getAttribute(EnhancedListener.ENHANCED_LISTENER_USED_ATTRIBUTE_NAME))) {
            WeldServletLogger.LOG.enhancedListenerUsedForNotifications();
            return;
        }
        WeldServletLogger.LOG.initializeWeldUsingServletContextListener();
        for (Consumer<ServletContext> initAction : initActions) {
            initAction.accept(context);
        }
        lifecycle = new WeldServletLifecycle();
        lifecycle.initialize(context);
        super.contextInitialized(sce);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (lifecycle == null) {
            if (!Boolean.TRUE
                    .equals(sce.getServletContext().getAttribute(EnhancedListener.ENHANCED_LISTENER_USED_ATTRIBUTE_NAME))) {
                // This should never happen
                WeldServletLogger.LOG.noServletLifecycleToDestroy();
            }
            return;
        }
        super.contextDestroyed(sce);
        lifecycle.destroy(sce.getServletContext());
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        if (lifecycle != null) {
            super.requestDestroyed(sre);
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        if (lifecycle != null) {
            super.requestInitialized(sre);
        }
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        if (lifecycle != null) {
            super.sessionCreated(se);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        if (lifecycle != null) {
            super.sessionDestroyed(se);
        }
    }

    @Override
    protected ServletListener delegate() {
        return lifecycle.getWeldListener();
    }

}
