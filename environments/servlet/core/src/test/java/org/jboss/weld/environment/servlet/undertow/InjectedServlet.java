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
package org.jboss.weld.environment.servlet.undertow;

import java.io.IOException;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InjectedServlet extends HttpServlet {

    private static final long serialVersionUID = -1627577238935505724L;

    @Inject
    private Foo field;
    private final Conversation constructor;

    @Inject
    public InjectedServlet(Conversation constructor) {
        this.constructor = constructor;
    }

    public InjectedServlet() {
        this(null);
    }

    @Inject
    public void init(BeanManager manager) {
        if (manager != null && field != null && constructor != null) {
            UndertowSmokeTest.SYNC.countDown();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Hello!");
    }
}
