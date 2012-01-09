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
package org.jboss.weld.tests.builtinBeans.ee;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ServletBuiltinBeanInjectingBean {

    @Inject
    private HttpServletRequest request;

    @Inject
    private HttpSession session;

    @Inject
    private ServletContext context;

    public void verifyRequest() {
        assert request.getRequestURI().contains("/request");
        assert request.getParameter("foo").equals("bar");
    }

    public void verifySession() {
        assert session.isNew();
        assert session.getAttribute("foo").equals("bar");
        session.setMaxInactiveInterval(60);
    }

    public void verifyServletContext() {
        assert context.getAttribute("foo").equals("bar");
        assert context.getServletRegistrations().size() == 1;
    }
}
