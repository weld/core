package org.jboss.weld.tests.servlet.dispatch;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/main/*")
public class MainServlet extends HttpServlet {

    @Inject
    private FirstBean bean;

    @Inject
    private TestBean tester;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().contains("dispatch")) {

            bean.getValue(); // just verify that the request context is active

            // if crossContext is set to true we are dispatching to app2, otherwise we are dispatching locally
            ServletContext ctx = req.getServletContext();
            if (Boolean.parseBoolean(req.getParameter("crossContext"))) {
                ctx = ctx.getContext("/app2");
            }

            if (req.getRequestURI().endsWith("/include")) {
                // /main/dispatch/include
                ctx.getRequestDispatcher("/content").include(req, resp);
            } else if (req.getRequestURI().endsWith("/forward")) {
                // /main/dispatch/forward
                ctx.getRequestDispatcher("/content").forward(req, resp);
            }
        } else if (req.getRequestURI().endsWith("/reset")) {
            tester.reset();
        } else if (req.getRequestURI().endsWith("/validate")) {
            resp.setContentType("text/plain");
            if (tester.isOk()) {
                resp.getWriter().write(String.valueOf(tester.isOk()));
            } else {
                resp.getWriter().write("constructions: " + tester.getConstructions() + ", destructions: " + tester.getDestructions());
            }
        }
    }
}
