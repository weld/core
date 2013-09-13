package org.jboss.weld.tests.contexts.request.filtering;

import java.io.IOException;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/")
public class FooServlet extends HttpServlet {

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
