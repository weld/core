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

import java.io.IOException;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/")
@SuppressWarnings("serial")
public class Servlet1 extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BeanManager manager = (BeanManager) req.getServletContext().getAttribute(BeanManager.class.getName());
        BeanManager listenerManager = (BeanManager) req.getServletContext().getAttribute(VerifyingListener.class.getName());
        
        resp.getWriter().append(getFoo(manager).ping());
        resp.getWriter().append(",");
        resp.getWriter().append(getFoo(listenerManager).ping());
        
        resp.setContentType("text/plain");
    }
    
    private Foo getFoo(BeanManager manager) {
        Bean<?> bean = manager.resolve(manager.getBeans(Foo.class));
        return (Foo) manager.getReference(bean, Foo.class, manager.createCreationalContext(null));
    }
}
