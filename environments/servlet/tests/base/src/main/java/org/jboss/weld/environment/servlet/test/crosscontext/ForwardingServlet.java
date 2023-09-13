package org.jboss.weld.environment.servlet.test.crosscontext;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ForwardingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext cx = getServletContext();
        ServletContext secondCx = cx.getContext("/app2");
        secondCx.getRequestDispatcher("/included").forward(req, resp);
    }
}