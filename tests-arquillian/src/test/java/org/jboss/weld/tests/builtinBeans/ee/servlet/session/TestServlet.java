/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.builtinBeans.ee.servlet.session;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(value = "/test", name = "testServlet")
@SuppressWarnings("serial")
public class TestServlet extends HttpServlet {

    @Inject
    private HttpSession httpSession;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // The invocation should force HttpSession creation
        String id = httpSession.getId();
        HttpSession requestSession = req.getSession(false);
        if (requestSession == null) {
            throw new IllegalStateException("HttpSession not created");
        }
        if (!id.equals(requestSession.getId())) {
            throw new IllegalStateException("Built-in HttpSession id: " + id
                    + " not equal to request HttpSession id: " + requestSession.getId());
        }
        resp.setContentType("text/plain");
        resp.getWriter().write(id);
    }
}
