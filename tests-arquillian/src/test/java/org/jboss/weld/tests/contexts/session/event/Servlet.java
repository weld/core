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
package org.jboss.weld.tests.contexts.session.event;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/")
@SuppressWarnings("serial")
public class Servlet extends HttpServlet {

    @Inject
    private ObservingBean observer;

    @Inject
    private SessionScopedBean bean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        bean.setFoo("baz"); // the session scope is initialized lazily
        if (req.getRequestURI().endsWith("/invalidate")) {
            int destroyed = observer.getDestroyedSessionCount().get();
            req.getSession().invalidate();
            if (destroyed != observer.getDestroyedSessionCount().get()) {
                throw new RuntimeException("@Destroyed(SessionScoped.class) called before the session context was actually destroyed");
            }
        }
        resp.getWriter().append("Initialized sessions:" + observer.getInitializedSessionCount().get());
        resp.getWriter().append("\n");
        resp.getWriter().append("Destroyed sessions:" + observer.getDestroyedSessionCount().get());
        resp.setContentType("text/plain");
    }
}
