/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.servlet.sessiondestroyed;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/introspect")
public class IntrospectServlet extends HttpServlet {

    public static final String MODE_INIT = "init";

    public static final String MODE_LONG_TASK = "long_task";

    public static final String MODE_DESTROY_SESSION = "destroy_session";

    private static final long LONG_TASK_TIMEOUT = 10000l;

    @Inject
    State state;

    @Inject
    SessionFoo foo;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/plain");
        String mode = req.getParameter("mode");

        if (MODE_INIT.equals(mode)) {
            state.reset();
            req.getSession();
            resp.getWriter().write(req.getSession().getId());
        } else if (MODE_LONG_TASK.equals(mode)) {
            long start = System.currentTimeMillis();
            while (!state.isSessionDestroyed() && (System.currentTimeMillis() - start < LONG_TASK_TIMEOUT)) {
                try {
                    Thread.sleep(100l);
                } catch (InterruptedException e) {
                    throw new IllegalStateException();
                }
            }
            foo.getId();
            resp.getWriter().write("OK");
        } else if (MODE_DESTROY_SESSION.equals(mode)) {
            state.setSessionDestroyed();
            req.getSession().invalidate();
            resp.getWriter().write("OK");
        }  else {
            throw new ServletException("Unknown test mode");
        }
    }

}
