package org.jboss.weld.probe.tests.integration.deployment;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.weld.probe.tests.integration.deployment.beans.ModelBean;

@WebServlet("/test")
public class InvokingServlet extends HttpServlet {

    @Inject
    ModelBean modelBean;


    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        modelBean.simpleCall();

        resp.getWriter().print("<html>");
        resp.getWriter().print("<body>");
        resp.getWriter().print(req.getSession().getId());
        resp.getWriter().print("</body>");
        resp.getWriter().print("</html>");
        resp.setContentType("text/html");

    }
}
