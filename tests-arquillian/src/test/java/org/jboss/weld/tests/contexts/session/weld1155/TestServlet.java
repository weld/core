/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.session.weld1155;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/")
public class TestServlet extends HttpServlet {

    private static final long serialVersionUID = -2993633416781607366L;

    @Inject
    private Product product;

    @Inject
    private SessionScopedBean bean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");

        // this forces session creation
        // we reuse the session in the next request

        if (!req.getRequestURI().endsWith("initial")) {
            try {
                product.ping();
            } catch (Exception e) {
                resp.setStatus(500);
                resp.getWriter().println(e.getMessage());
                return;
            }
        }
        // this check is not necessary but just in case...
        if (bean.getId() > 1) {
            resp.setStatus(500);
            resp.getWriter().print("Bean id > 1");
            return;
        }
        resp.getWriter().println("Here you are: " + bean.getId());
    }
}
