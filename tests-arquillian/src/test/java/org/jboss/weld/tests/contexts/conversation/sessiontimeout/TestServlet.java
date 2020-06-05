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

package org.jboss.weld.tests.contexts.conversation.sessiontimeout;

import java.io.IOException;

import jakarta.enterprise.context.Conversation;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.weld.test.util.ActionSequence;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/*")
public class TestServlet extends HttpServlet {

    @Inject
    private Foo foo;

    @Inject
    private Conversation conversation;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        if ("/init".equals(req.getPathInfo())) {
            ActionSequence.reset();
            req.getSession(true);
            req.getSession().setMaxInactiveInterval(1);
            conversation.begin();
            foo.ping();
            resp.getWriter().println(conversation.getId());
        } else {
            // we waited for >1 sec so session should timeout
            resp.getWriter().println(ActionSequence.getSequence().dataToCsv());
        }
    }
}
