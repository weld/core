package org.jboss.weld.tests.servlet;

import java.io.IOException;

import jakarta.enterprise.context.Dependent;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@Dependent
public class Bar implements Filter {

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

}
