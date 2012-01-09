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
package org.jboss.weld.tests.contexts.conversation.event;

import java.io.IOException;

import javax.enterprise.context.Conversation;
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
    private Conversation conversation;

    @Inject
    private ConversationScopedBean bean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri.endsWith("/begin")) {
            conversation.begin("org.jboss.weld");
            bean.setFoo("baz");
        } else if (uri.contains("/end")) {
            conversation.end();
        }
        resp.getWriter().append("Initialized conversations:" + observer.getInitializedConversationCount().get());
        resp.getWriter().append("\n");
        resp.getWriter().append("Destroyed conversations:" + observer.getDestroyedConversationCount().get());
        resp.getWriter().append("\n");
        resp.getWriter().append("cid:" + conversation.getId());
        resp.setContentType("text/plain");
    }
}
