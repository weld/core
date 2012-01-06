/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.beanManager.access.web;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Verifies that the correct BeanManager can be obtained from the ServletContext early in the deployment - during
 * ServletContextListener notification.
 * 
 * @author Jozef Hartinger
 * 
 */
@WebListener
public class VerifyingListener implements ServletContextListener {

    private boolean invoked;

    public void contextInitialized(ServletContextEvent sce) {
        if (invoked) {
            throw new IllegalStateException("ServletContextEvent observed multiple times");
        }
        invoked = true;
        // we can obtain the manager but cannot verify if this is the correct one
        // we'll pass it to the servlet under a different namespace to verify
        BeanManager manager = (BeanManager) sce.getServletContext().getAttribute(BeanManager.class.getName());
        if (manager == null) {
            throw new IllegalStateException("BeanManager not available in ServletContext");
        }
        sce.getServletContext().setAttribute(VerifyingListener.class.getName(), manager);
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
