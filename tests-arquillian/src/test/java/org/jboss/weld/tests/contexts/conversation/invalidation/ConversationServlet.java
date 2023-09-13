/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.conversation.invalidation;

import java.io.IOException;

import jakarta.enterprise.context.Conversation;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.weld.test.util.ActionSequence;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/*")
public class ConversationServlet extends HttpServlet {

    @Inject
    ConversationBean bean;

    @Inject
    Conversation conversation;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/begin".equals(req.getPathInfo())) {
            req.getSession(true);
            conversation.begin();
            bean.pong();
            resp.getWriter().print(conversation.getId());
        } else if ("/result".equals(req.getPathInfo())) {
            // invoke as last (and separate) request to get complete list of actions
            resp.getWriter().print(ActionSequence.getSequence().dataToCsv());
        } else if ("/resetSequence".equals(req.getPathInfo())) {
            ActionSequence.reset();
        } else {
            ActionSequence.addAction("beforeInvalidate");
            req.getSession().invalidate();
            ActionSequence.addAction("afterInvalidate");
            resp.getWriter().print("Session invalidated");
        }
    }
}
