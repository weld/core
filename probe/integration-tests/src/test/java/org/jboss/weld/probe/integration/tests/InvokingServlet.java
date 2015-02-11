package org.jboss.weld.probe.integration.tests;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/test")
public class InvokingServlet extends HttpServlet {

    @Inject
    ModelBean modelBean;


    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        modelBean.simpleCall();

        resp.getWriter().print(req.getSession().getId());
        resp.setContentType("text/html");

    }
}
