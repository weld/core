/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.servlet.test.lifecycle;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class HSListener implements HttpSessionListener {

    private BeanManager getBeanManager() {
        Context context = null;
        try {
            context = new InitialContext();
            return (BeanManager) context.lookup("java:comp/env/BeanManager");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (context != null)
                try {
                    context.close();
                } catch (NamingException ignored) {
                }
        }
    }

    public void sessionCreated(HttpSessionEvent se) {
        getBeanManager().fireEvent(se.getSession());
    }

    public void sessionDestroyed(HttpSessionEvent se) {
    }
}