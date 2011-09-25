package org.jboss.weld.environment.servlet.test.injection;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CatFilter implements Filter {

    @Inject
    Sewer sewer;

    public void init(FilterConfig filterConfig) throws ServletException {
        isSewerNameOk();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ((HttpServletResponse) response).setStatus(isSewerNameOk() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    public void destroy() {
        isSewerNameOk();
    }

    private boolean isSewerNameOk() throws NullPointerException {
        return Sewer.NAME.equals(sewer.getName());
    }
}
