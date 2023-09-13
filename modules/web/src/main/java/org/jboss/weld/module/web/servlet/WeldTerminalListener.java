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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import org.jboss.weld.Container;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.BeanManagers;
import org.jboss.weld.module.web.context.http.HttpSessionDestructionContext;

/**
 * This listener activates the HttpSessionDestructionContext in sessionDestroyed(), but only if HttpSessionContext is not
 * active at the time (it is active when sessionDestroyed is invoked as a result of session.invalidate() during an
 * http request, and is not active when sessionDestroyed is invoked when the session times out or when all the sessions
 * are destroyed because the deployment is being removed).
 *
 * This listener should always be the last registered listener. This will ensure this listener can activate the
 * session context before any other listeners try to access it (the listeners are notified in reverse order when a
 * session is being destroyed). Any listeners notified before this one will receive a ContextNotActiveException when
 * trying to access any @SessionScoped bean.
 *
 * @author Marko Luksa
 */
public class WeldTerminalListener implements HttpSessionListener {

    @Inject
    private volatile BeanManagerImpl beanManager;

    public WeldTerminalListener() {
    }

    public WeldTerminalListener(BeanManagerImpl beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        final ServletContext ctx = event.getSession().getServletContext();
        // First try to use the context id obtained from the servlet context (OSGi, Servlet containers, etc.)
        if (beanManager == null) {
            synchronized (this) {
                if (beanManager == null) {
                    String contextId = ctx.getInitParameter(Container.CONTEXT_ID_KEY);
                    if (contextId != null) {
                        List<BeanManagerImpl> managers = new ArrayList<BeanManagerImpl>(
                                Container.instance(contextId).beanDeploymentArchives().values());
                        Collections.sort(managers, BeanManagers.ID_COMPARATOR);
                        beanManager = managers.get(0);
                    }
                }
                // servlet containers may not be able to inject fields in a servlet listener
                if (beanManager == null) {
                    beanManager = BeanManagerProxy.unwrap(CDI.current().getBeanManager());
                }
            }
        }

        if (!getSessionContext().isActive()) {
            HttpSessionDestructionContext context = getHttpSessionDestructionContext();
            context.associate(event.getSession());
            context.activate();
        }
    }

    private HttpSessionContext getSessionContext() {
        return beanManager.instance().select(HttpSessionContext.class).get();
    }

    private HttpSessionDestructionContext getHttpSessionDestructionContext() {
        return beanManager.instance().select(HttpSessionDestructionContext.class).get();
    }
}
