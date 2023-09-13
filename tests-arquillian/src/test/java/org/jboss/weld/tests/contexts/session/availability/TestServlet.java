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
package org.jboss.weld.tests.contexts.session.availability;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/test")
public class TestServlet extends HttpServlet {

    @Inject
    RequestScopedBean requestScopedBean;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean change = Boolean.valueOf(req.getParameter("change"));
        boolean set = Boolean.valueOf(req.getParameter("set"));
        boolean print = Boolean.valueOf(req.getParameter("print"));

        if (set) {
            requestScopedBean.increment();
            resp.getWriter().print(req.getSession().getId());
        }

        if (change) {
            resp.getWriter().print(req.changeSessionId());
        }
        if (print) {
            resp.getWriter().print(requestScopedBean.getDataFromSession());
        }
    }

}
