package org.jboss.weld.tests.contexts.request.filtering;

import java.io.IOException;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/")
public class BarServlet extends HttpServlet {

    @Inject
    private Foo foo;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.getWriter().print(isContextActive());
    }

    private boolean isContextActive() {
        try {
            foo.ping();
            return true;
        } catch (ContextNotActiveException e) {
            return false;
        }
    }
}
