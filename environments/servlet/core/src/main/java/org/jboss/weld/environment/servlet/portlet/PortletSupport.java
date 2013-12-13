/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.portlet;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.environment.servlet.WeldServletLifecycle;

/**
 * Simple portlet support.
 *
 * @author Marko Strukelj
 * @author Ales Justin
 */
public final class PortletSupport {
    private static volatile Boolean enabled;

    private PortletSupport() {
    }

    /**
     * Is portlet env supported.
     *
     * @return true if portlet env is supported, false otherwise
     */
    public static boolean isPortletEnvSupported() {
        if (enabled == null) {
            synchronized (PortletSupport.class) {
                if (enabled == null) {
                    try {
                        PortletSupport.class.getClassLoader().loadClass("javax.portlet.PortletContext");
                        enabled = true;
                    } catch (Throwable ignored) {
                        enabled = false;
                    }
                }
            }
        }
        return enabled;
    }

    /**
     * Is the ctx object instance of portlet context.
     *
     * @param ctx the current context
     * @return true is portlet context, false otherwise
     */
    public static boolean isPortletContext(Object ctx) {
        return (ctx instanceof javax.portlet.PortletContext);
    }

    /**
     * Get bean manager from portlet context.
     *
     * @param ctx the portlet context
     * @return bean manager if found
     */
    public static BeanManager getBeanManager(Object ctx) {
        return (BeanManager) javax.portlet.PortletContext.class.cast(ctx).getAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME);
    }
}
