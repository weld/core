package org.jboss.weld.tests.servlet.dispatch;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/content")
public class FirstServlet extends HttpServlet {

    @Inject
    private FirstBean bean;

    @Inject
    private FirstConversationScopedBean conversation;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.getWriter().write(bean.getValue() + ":" + conversation.getValue());
    }
}
