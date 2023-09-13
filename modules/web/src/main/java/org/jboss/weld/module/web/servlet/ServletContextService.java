/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.security.AccessController;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletContext;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.security.GetContextClassLoaderAction;

/**
 * Simple holder for {@link ServletContext}s that associates a ServletContext with the TCCL.
 *
 * @author Jozef Hartinger
 *
 */
public class ServletContextService implements Service {

    private final Map<ClassLoader, ServletContext> servletContexts = new ConcurrentHashMap<ClassLoader, ServletContext>();

    void contextInitialized(ServletContext context) {
        final ClassLoader cl = getContextClassLoader();
        if (cl != null) {
            servletContexts.put(cl, context);
        }
    }

    /**
     * Obtains the {@link ServletContext} associated with this request.
     *
     * @return the ServletContext associated with this request or null if there is no such association
     */
    public ServletContext getCurrentServletContext() {
        final ClassLoader cl = getContextClassLoader();
        if (cl == null) {
            return null;
        }
        return servletContexts.get(cl);
    }

    private ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return GetContextClassLoaderAction.INSTANCE.run();
        } else {
            return AccessController.doPrivileged(GetContextClassLoaderAction.INSTANCE);
        }

    }

    @Override
    public void cleanup() {
        servletContexts.clear();
    }

    @Override
    public String toString() {
        return "ServletContextService [" + servletContexts + "]";
    }
}
