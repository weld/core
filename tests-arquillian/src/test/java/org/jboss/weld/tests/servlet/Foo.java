package org.jboss.weld.tests.servlet;

import java.io.IOException;

import jakarta.enterprise.context.Dependent;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@Dependent
public class Foo implements Servlet {

    public void destroy() {
    }

    public ServletConfig getServletConfig() {
        return null;
    }

    public String getServletInfo() {
        return null;
    }

    public void init(ServletConfig config) throws ServletException {
    }

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    }

}
