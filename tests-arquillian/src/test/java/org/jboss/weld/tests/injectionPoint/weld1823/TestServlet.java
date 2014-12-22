/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.tests.injectionPoint.weld1823;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 *@author Emily Jiang
 */
@WebServlet("/ProducerNullIPServlet")
public class TestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    @Inject
    private CDIBean bean;

    @Inject
    private BeanManager manager;

    @SuppressWarnings("unused")
    @Inject
    private Instance<CDIBean2> anotherBeanInstance;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        PrintWriter out = resp.getWriter();

        Set<Bean<?>> beanList = manager.getBeans(CDIBean2.class);
        if (beanList != null && !beanList.isEmpty()) {
            Bean<?> bean = beanList.iterator().next();
            CreationalContext<?> context = manager.createCreationalContext(bean);
            CDIBean2 beanManagerInstance = (CDIBean2) manager.getReference(bean, CDIBean2.class, context);
            beanManagerInstance.setData("Test2");
        }
        out.println("Test Sucessful!");
    }
}
