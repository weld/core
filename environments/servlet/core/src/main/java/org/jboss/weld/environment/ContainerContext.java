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

package org.jboss.weld.environment;

import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.manager.api.WeldManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Wrap listener arguments.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ContainerContext {
    private ServletContextEvent event;
    private ServletContext context;
    private WeldManager manager;

    public ContainerContext(ServletContextEvent event, WeldManager manager) {
        if (event == null) {
            throw new IllegalArgumentException("Null servlet context event");
        }

        this.event = event;
        this.context = event.getServletContext();
        if (manager == null) {
            manager = (WeldManager) context.getAttribute(Listener.BEAN_MANAGER_ATTRIBUTE_NAME);
        }

        this.manager = manager;
    }

    public ServletContextEvent getEvent() {
        return event;
    }

    public ServletContext getContext() {
        return context;
    }

    public WeldManager getManager() {
        return manager;
    }
}
