/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.contexts.conversation.timeout;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/servlet/*")
public class TimeoutServlet extends HttpServlet {

    @Inject
    private TimeoutBean timeoutBean;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/beginConversation".equals(req.getPathInfo())) {
            String cid = timeoutBean.beginConversation();
            resp.getWriter().print(cid);
        } else if ("/testConversation".equals(req.getPathInfo())) {
            String cid = timeoutBean.testConversation();
            resp.getWriter().print(cid);
        } else if ("/makeLongRequest".equals(req.getPathInfo())) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new ServletException("Unable to sleep thread for long request", e);
            }
            String cid = timeoutBean.testConversation();
            resp.getWriter().print(cid);
        }
    }
}
