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
package org.jboss.weld.tests.builtinBeans.ee.servlet;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Dependent
public class ServletBuiltinBeanInjectingBean {

    @Inject
    private HttpServletRequest request;

    @Inject
    private HttpSession session;

    @Inject
    private ServletContext context;

    public void verifyRequest() {
        assert request.getRequestURI().contains("/request");
        assert request.getParameter("foo").equals("bar");
    }

    public void verifySession() {
        assert session.isNew();
        assert session.getAttribute("foo").equals("bar");
        session.setMaxInactiveInterval(60);
    }

    public void verifyServletContext() {
        assert context.getAttribute("foo").equals("bar");
        assert context.getServletRegistration("testServlet") != null;
        assert Servlet.class.getName().equals(context.getServletRegistration("testServlet").getClassName());
    }
}
