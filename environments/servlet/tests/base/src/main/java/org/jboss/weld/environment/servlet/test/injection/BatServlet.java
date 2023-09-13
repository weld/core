package org.jboss.weld.environment.servlet.test.injection;

import static org.jboss.weld.environment.servlet.test.injection.BatListener.BAT_ATTRIBUTE_NAME;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class BatServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String mode = req.getParameter("mode");
        int status;
        if ("request".equals(mode)) {
            status = Boolean.TRUE.equals(req.getAttribute(BAT_ATTRIBUTE_NAME)) ? HttpServletResponse.SC_OK
                    : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        } else if ("sce".equals(mode)) {
            status = Boolean.TRUE.equals(req.getServletContext().getAttribute(BAT_ATTRIBUTE_NAME)) ? HttpServletResponse.SC_OK
                    : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        } else if ("session".equals(mode)) {
            status = Boolean.TRUE.equals(req.getSession().getAttribute(BAT_ATTRIBUTE_NAME)) ? HttpServletResponse.SC_OK
                    : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        } else {
            status = HttpServletResponse.SC_NOT_FOUND;
        }
        resp.setStatus(status);
    }

}
