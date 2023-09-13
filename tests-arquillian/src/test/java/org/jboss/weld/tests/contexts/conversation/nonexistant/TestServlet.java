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

package org.jboss.weld.tests.contexts.conversation.nonexistant;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/*")
public class TestServlet extends HttpServlet {

    @Inject
    ConversationBean conversation;

    @Inject
    ConversationScopedBean bean;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        if ("/begin".equals(req.getPathInfo())) {
            // start conversation, touch scoped bean and end it
            conversation.begin(req);
            bean.getMsg();
            conversation.end();
        } else if ("/init".equals(req.getPathInfo())) {
            // return time @Initialized was fired in total
            resp.getWriter().print("init: " + ConversationBean.TIMES_INIT_INVOKED);
        } else {
            // return time @Destroyed was fired in total
            resp.getWriter().println("destroyed: " + ConversationBean.TIMES_DESTROYED_INVOKED);
        }
    }
}
