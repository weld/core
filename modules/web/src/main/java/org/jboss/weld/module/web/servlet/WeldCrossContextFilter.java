package org.jboss.weld.module.web.servlet;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletResponse;

/**
 * This class provides support for cross-context dispatching to a web application that's using Weld.
 * By default Weld assumes single context dispatching, and relies upon receiving events accordingly.
 * <p>
 * </p>
 * This filter is only required in special circumstances - i.e. within portlet applications.
 * <p>
 * </p>
 * To install add the following configuration to your portlet web archive's web.xml:
 *
 * <pre>
 *   <filter>
 *       <filter-name>WeldCrossContextFilter</filter-name>
 *       <filter-class>org.jboss.weld.servlet.WeldCrossContextFilter</filter-class>
 *   </filter>
 *
 *   <filter-mapping>
 *       <filter-name>WeldCrossContextFilter</filter-name>
 *       <url-pattern>/*</url-pattern>
 *       <dispatcher>INCLUDE</dispatcher>
 *       <dispatcher>FORWARD</dispatcher>
 *       <dispatcher>ERROR</dispatcher>
 *   </filter-mapping>
 * </pre>
 *
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class WeldCrossContextFilter implements Filter {
    private static final String REQUEST_CONTEXT_KEY = "org.jboss.weld.context.http.HttpRequestContextImpl";
    private volatile WeldInitialListener listener;
    private FilterConfig config;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (listener == null) {
            listener = (WeldInitialListener) request.getServletContext().getAttribute(WeldInitialListener.class.getName());
        }

        // cross-context means request is dispatched as INCLUDE or FORWARD or ERROR
        boolean crossCtx = request.getAttribute("jakarta.servlet.include.request_uri") != null
                || request.getAttribute("jakarta.servlet.forward.request_uri") != null
                || request.getAttribute("jakarta.servlet.error.request_uri") != null;

        boolean activated = false;
        try {
            if (crossCtx) {
                if (request.getAttribute(REQUEST_CONTEXT_KEY) == null) {
                    listener.requestInitialized(new ServletRequestEvent(config.getServletContext(), request));
                    activated = true;
                }
            }
            chain.doFilter(request, response);

        } finally {
            if (activated) {
                listener.requestDestroyed(new ServletRequestEvent(config.getServletContext(), request));
            }
        }
    }

    @Override
    public void destroy() {
    }
}