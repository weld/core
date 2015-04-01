/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.probe;

import static org.jboss.weld.probe.Strings.TEXT_HTML;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Invocation.Type;
import org.jboss.weld.probe.InvocationMonitor.Action;
import org.jboss.weld.util.reflection.Formats;

/**
 * This servlet filter enables clippy-like support and allows to group together monitored invocations within the same request.
 *
 * <p>
 * An integrator is required to register this extension if appropriate.
 * </p>
 *
 * <p>
 * To disable the clippy support, set {@link ConfigurationKey#PROBE_CLIPPY_SUPPORT} to <code>false</code>. It's also possible to use the
 * {@link ConfigurationKey#PROBE_INVOCATION_MONITOR_EXCLUDE_TYPE} to skip the monitoring.
 * </p>
 *
 * @see ConfigurationKey#PROBE_CLIPPY_SUPPORT
 * @see ConfigurationKey#PROBE_INVOCATION_MONITOR_EXCLUDE_TYPE
 * @author Martin Kouba
 */
public class ProbeFilter implements Filter {

    @Inject
    private BeanManagerImpl beanManager;

    // It shouldn't be necessary to make these fields volatile - see also javax.servlet.GenericServlet.config
    private String clippy;

    private Probe probe;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        if (beanManager == null) {
            throw ProbeLogger.LOG.probeFilterUnableToOperate(BeanManagerImpl.class);
        }

        Probe probe = beanManager.getServices().get(Probe.class);
        if (probe == null) {
            throw ProbeLogger.LOG.probeFilterUnableToOperate(Probe.class);
        }

        WeldConfiguration configuration = beanManager.getServices().get(WeldConfiguration.class);

        // Note that we have to use in-line CSS
        if (configuration.getBooleanProperty(ConfigurationKey.PROBE_CLIPPY_SUPPORT)) {
            StringBuilder builder = new StringBuilder();
            builder.append("<!-- The following snippet was automatically added by Weld, see the documentation to disable this functionality -->");
            builder.append("<div id=\"weld-dev-mode-info\" style=\"position: fixed !important;bottom:0;left:0;width:100%;background-color:#f8f8f8;border:2px solid silver;padding:10px;border-radius:2px;margin:0px;font-size:16px;font-family:sans-serif;color:black;\">");
            builder.append("<img alt=\"Weld logo\" src=\"");
            builder.append(filterConfig.getServletContext().getContextPath());
            builder.append("/weld-probe/client/weld_icon_48x.png\">");
            builder.append("&nbsp; Running on Weld <span style=\"color:gray\">");
            builder.append(Formats.version(WeldBootstrap.class.getPackage().getSpecificationVersion(), null));
            builder.append("</span>. The development mode is <span style=\"color:white;background-color:#d62728;padding:6px;border-radius:4px;font-size:14px;\">ENABLED</span>. Inspect your application with <a style=\"color:#337ab7;font-weight:bold;\" href=\"");
            builder.append(filterConfig.getServletContext().getContextPath());
            builder.append("/weld-probe");
            builder.append("\" target=\"_blank\">Probe Development Tool</a>.");
            builder.append(" <button style=\"background-color:#f8f8f8;border:1px solid silver; color:gray;border-radius:4px;padding:4px 10px 4px 10px;margin-left:2em;font-weight: bold;\" onclick=\"document.getElementById('weld-dev-mode-info').style.display='none';\">x</button></div>");
            clippy = builder.toString();
        }

        String exclude = configuration.getStringProperty(ConfigurationKey.PROBE_INVOCATION_MONITOR_EXCLUDE_TYPE);
        if (exclude.isEmpty() || !Pattern.compile(exclude).matcher(ProbeFilter.class.getName()).matches()) {
            this.probe = probe;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (httpServletRequest.getServletPath().startsWith(ProbeServlet.URL_PATTERN_BASE)) {
            // Don't apply the filter to the probe servlet
            chain.doFilter(request, response);
            return;
        }

        final Invocation.Builder builder;
        if (probe != null) {
            builder = InvocationMonitor.initBuilder();
            builder.setDeclaringClassName(ProbeFilter.class.getName());
            builder.setStart(System.currentTimeMillis());
            builder.setMethodName("doFilter");
            builder.setType(Type.BUSINESS);
            builder.setDescription(getDescription(httpServletRequest));
            builder.ignoreIfNoChildren();
        } else {
            builder = null;
        }

        if (clippy == null) {
            FilterAction.of(request, response).doFilter(builder, probe, chain);
        } else {
            ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
            FilterAction.of(request, responseWrapper).doFilter(builder, probe, chain);
            String captured = responseWrapper.getOutput();

            if (captured != null && !captured.isEmpty()) {
                // Writer was used
                PrintWriter out = response.getWriter();
                if (response.getContentType() != null && response.getContentType().startsWith(TEXT_HTML)) {
                    int idx = captured.indexOf("</body>");
                    if (idx == -1) {
                        // </body> not found
                        out.write(captured);
                    } else {
                        CharArrayWriter writer = new CharArrayWriter();
                        writer.write(captured.substring(0, idx));
                        writer.write(clippy);
                        writer.write(captured.substring(idx, captured.length()));
                        out.write(writer.toString());
                    }
                } else {
                    out.write(captured);
                }
            }
        }
    }

    @Override
    public void destroy() {
    }

    private String getDescription(HttpServletRequest req) {
        StringBuilder builder = new StringBuilder();
        builder.append(req.getMethod());
        builder.append(' ');
        builder.append(req.getRequestURI());
        String queryString = req.getQueryString();
        if (queryString != null) {
            builder.append('?');
            builder.append(queryString);
        }
        return builder.toString();
    }

    private static class ResponseWrapper extends HttpServletResponseWrapper {

        private final CharArrayWriter output;

        private final PrintWriter writer;

        ResponseWrapper(HttpServletResponse response) {
            super(response);
            output = new CharArrayWriter();
            writer = new PrintWriter(output);
        }

        public PrintWriter getWriter() {
            return writer;
        }

        String getOutput() {
            return output.toString();
        }
    }

    private static class FilterAction extends Action<FilterChain> {

        private static FilterAction of(ServletRequest request, ServletResponse response) {
            return new FilterAction(request, response);
        }

        private final ServletRequest request;

        private final ServletResponse response;

        private FilterAction(ServletRequest request, ServletResponse response) {
            this.request = request;
            this.response = response;
        }

        @Override
        protected Object proceed(FilterChain chain) throws Exception {
            chain.doFilter(request, response);
            return null;
        }

        void doFilter(Invocation.Builder builder, Probe probe, FilterChain chain) throws ServletException, IOException {
            if (builder == null) {
                chain.doFilter(request, response);
            } else {
                try {
                    perform(builder, probe, chain);
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }
        }

    }

}
