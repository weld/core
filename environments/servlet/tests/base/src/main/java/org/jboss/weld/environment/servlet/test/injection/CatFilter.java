package org.jboss.weld.environment.servlet.test.injection;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

public class CatFilter implements Filter {

    @Inject
    Sewer sewer;

    public void init(FilterConfig filterConfig) throws ServletException {
        isSewerNameOk();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ((HttpServletResponse) response)
                .setStatus(isSewerNameOk() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    public void destroy() {
        isSewerNameOk();
    }

    private boolean isSewerNameOk() throws NullPointerException {
        return Sewer.NAME.equals(sewer.getName());
    }
}
