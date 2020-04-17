/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.session.destroy;

import java.io.IOException;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/test")
public class TestServlet extends HttpServlet {

    @Inject
    SessionScopedBean bean;

    @Inject
    BeanManager beanManager;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        switch (action) {
            case "init":
                resp.getWriter().print(bean.ping());
                break;
            case "destroy":
                Bean<?> bean = beanManager.getBeans(SessionScopedBean.class).iterator().next();
                AlterableContext sessionContext = (AlterableContext) beanManager.getContext(SessionScoped.class);
                sessionContext.destroy(bean);
                resp.getWriter().print("ok");
                break;
            case "test":
                resp.getWriter().print(SessionScopedBean.DESTROYED);
                break;
            default:
                throw new IllegalStateException("Unknown action");
        }
    }

}
