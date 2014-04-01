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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.jboss.logging.Logger;
import org.jboss.weld.servlet.api.ServletListener;
import org.jboss.weld.servlet.api.helpers.ForwardingServletListener;

/**
 * This is the original listener which had to be defined in web.xml.
 *
 * It's not necessary to register this listener in Servlet 3.0 compliant containers unless there are listener ordering conflicts. E.g. if a user provides a
 * custom listener the request context will not be active during its notifications. In this case place this listener before any other listener definitions in
 * web.xml.
 *
 * ServletContext notifications are no-op in case of the {@link EnhancedListener} is registered as well.
 *
 * @author Pete Muir
 * @author Ales Justin
 * @see EnhancedListener
 */
public class Listener extends ForwardingServletListener {

    public static final String LISTENER_USED_ATTRIBUTE_NAME = EnhancedListener.class.getPackage().getName() + ".listenerUsed";

    private static final Logger log = Logger.getLogger(Listener.class);

    private boolean isEnhancedListenerUsed;

    private WeldServletLifecycle lifecycle;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        lifecycle = (WeldServletLifecycle) context.getAttribute(WeldServletLifecycle.INSTANCE_ATTRIBUTE_NAME);
        if(lifecycle != null) {
            isEnhancedListenerUsed = true;
        }
        context.setAttribute(LISTENER_USED_ATTRIBUTE_NAME, Boolean.TRUE);
        if (isEnhancedListenerUsed) {
            log.info("org.jboss.weld.environment.servlet.EnhancedListener used for ServletContext notifications");
            return;
        }
        log.info("Initialize Weld using ServletContextListener");
        lifecycle = new WeldServletLifecycle();
        lifecycle.initialize(context);
        super.contextInitialized(sce);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (isEnhancedListenerUsed) {
            return;
        }
        lifecycle.destroy(sce.getServletContext());
        super.contextDestroyed(sce);
    }

    @Override
    protected ServletListener delegate() {
        return lifecycle.getWeldListener();
    }

}
