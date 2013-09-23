package org.jboss.weld.environment.servlet.test.crosscontext;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ForwardingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext cx =getServletContext();
        ServletContext secondCx=cx.getContext("/app2");
        secondCx.getRequestDispatcher("/included").forward(req, resp);
    }
}