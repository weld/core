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
package org.jboss.weld.tests.contexts.session.event;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/")
@SuppressWarnings("serial")
public class Servlet extends HttpServlet {

    @Inject
    private ObservingBean observer;

    @Inject
    private SessionScopedBean bean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        bean.setFoo("baz"); // the session scope is initialized lazily
        if (req.getRequestURI().endsWith("/invalidate")) {
            int destroyed = observer.getDestroyedSessionCount().get();
            req.getSession().invalidate();
            if (destroyed != observer.getDestroyedSessionCount().get()) {
                throw new RuntimeException(
                        "@Destroyed(SessionScoped.class) called before the session context was actually destroyed");
            }
        }
        resp.getWriter().append("Initialized sessions:").append(Integer.toString(observer.getInitializedSessionCount().get()));
        resp.getWriter().append('\n');
        resp.getWriter().append("Destroyed sessions:").append(Integer.toString(observer.getDestroyedSessionCount().get()));
        resp.setContentType("text/plain");
    }
}
