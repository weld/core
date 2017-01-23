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

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Invocation.Type;
import org.jboss.weld.probe.InvocationMonitor.Action;
import org.jboss.weld.probe.Resource.HttpMethod;
import org.jboss.weld.util.reflection.Formats;

/**
 * This filter takes care of the servlet integration. Basically, it:
 *
 * <ul>
 * <li>implements a simple REST API,</li>
 * <li>allows to group together monitored invocations within the same request,</li>
 * <li>allows to embed an informative HTML snippet to every response with Content-Type of value <code>text/html</code>.</li>
 * </ul>
 *
 * <p>
 * An integrator is required to register this filter. The filter should only be mapped to a single URL pattern of value <code>/*</code>.
 * </p>
 *
 * <p>
 * To disable the clippy support, set {@link ConfigurationKey#PROBE_EMBED_INFO_SNIPPET} to <code>false</code>. It's also possible to use the
 * {@link ConfigurationKey#PROBE_INVOCATION_MONITOR_EXCLUDE_TYPE} to skip the monitoring.
 * </p>
 *
 * @see ConfigurationKey#PROBE_EMBED_INFO_SNIPPET
 * @see ConfigurationKey#PROBE_INVOCATION_MONITOR_EXCLUDE_TYPE
 * @author Martin Kouba
 */
@Vetoed
public class ProbeFilter implements Filter {

    static final String REST_URL_PATTERN_BASE = "/weld-probe";

    static final String WELD_SERVLET_BEAN_MANAGER_KEY = "org.jboss.weld.environment.servlet.javax.enterprise.inject.spi.BeanManager";

    @Inject
    private BeanManagerImpl beanManager;

    // It shouldn't be necessary to make these fields volatile - see also javax.servlet.GenericServlet.config
    private String snippetBase;

    private Probe probe;

    private JsonDataProvider jsonDataProvider;

    private boolean skipMonitoring;

    private Pattern allowRemoteAddressPattern;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        if (beanManager == null) {
            beanManager = BeanManagerProxy.tryUnwrap(filterConfig.getServletContext().getAttribute(WELD_SERVLET_BEAN_MANAGER_KEY));
            if (beanManager == null) {
                throw ProbeLogger.LOG.probeFilterUnableToOperate(BeanManagerImpl.class);
            }
        }

        ProbeExtension probeExtension = beanManager.getExtension(ProbeExtension.class);
        if (probeExtension == null) {
            throw ProbeLogger.LOG.probeFilterUnableToOperate(ProbeExtension.class);
        }
        probe = probeExtension.getProbe();
        if (!probe.isInitialized()) {
            throw ProbeLogger.LOG.probeNotInitialized();
        }
        jsonDataProvider = probeExtension.getJsonDataProvider();

        WeldConfiguration configuration = beanManager.getServices().get(WeldConfiguration.class);

        if (configuration.getBooleanProperty(ConfigurationKey.PROBE_EMBED_INFO_SNIPPET)) {
            snippetBase = initSnippetBase(filterConfig.getServletContext());
        }

        String exclude = configuration.getStringProperty(ConfigurationKey.PROBE_INVOCATION_MONITOR_EXCLUDE_TYPE);
        skipMonitoring = !exclude.isEmpty() && Pattern.compile(exclude).matcher(ProbeFilter.class.getName()).matches();

        String allowRemoteAddress = configuration.getStringProperty(ConfigurationKey.PROBE_ALLOW_REMOTE_ADDRESS);
        allowRemoteAddressPattern = allowRemoteAddress.isEmpty() ? null : Pattern.compile(allowRemoteAddress);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (allowRemoteAddressPattern != null && !allowRemoteAddressPattern.matcher(request.getRemoteAddr()).matches()) {
            ProbeLogger.LOG.requestDenied(httpRequest.getRequestURI(), request.getRemoteAddr());
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final String[] resourcePathParts = getResourcePathParts(httpRequest.getRequestURI(), httpRequest.getServletContext().getContextPath());

        if (resourcePathParts != null) {
            // Probe resource
            HttpMethod method = HttpMethod.from(httpRequest.getMethod());
            if (method == null) {
                // Unsupported protocol
                if (httpRequest.getProtocol().endsWith("1.1")) {
                    httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                } else {
                    httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
                return;
            }
            processResourceRequest(httpRequest, httpResponse, method, resourcePathParts);
        } else {
            // Application request - init monitoring and embed info snippet if required
            final Invocation.Builder builder;
            if (!skipMonitoring) {
                // Don't initialize a new builder if an entry point already exists
                builder = InvocationMonitor.initBuilder(false);
                if (builder != null) {
                    builder.setDeclaringClassName(ProbeFilter.class.getName());
                    builder.setStart(System.currentTimeMillis());
                    builder.setMethodName("doFilter");
                    builder.setType(Type.BUSINESS);
                    builder.setDescription(getDescription(httpRequest));
                    builder.ignoreIfNoChildren();
                }
            } else {
                builder = null;
            }
            if (snippetBase == null) {
                FilterAction.of(request, response).doFilter(builder, probe, chain);
            } else {
                embedInfoSnippet(httpRequest, httpResponse, builder, chain);
            }
        }
    }

    @Override
    public void destroy() {
    }

    private void embedInfoSnippet(HttpServletRequest req, HttpServletResponse resp, Invocation.Builder builder, FilterChain chain)
            throws IOException, ServletException {
        ResponseWrapper responseWrapper = new ResponseWrapper(resp);
        FilterAction.of(req, responseWrapper).doFilter(builder, probe, chain);
        String captured = responseWrapper.getOutput();
        if (captured != null && !captured.isEmpty()) {
            // Writer was used
            PrintWriter out = resp.getWriter();
            if (resp.getContentType() != null && resp.getContentType().startsWith(TEXT_HTML)) {
                int idx = captured.indexOf("</body>");
                if (idx == -1) {
                    // </body> not found
                    out.write(captured);
                } else {
                    CharArrayWriter writer = new CharArrayWriter();
                    writer.write(captured.substring(0, idx));
                    writer.write(snippetBase);
                    if (builder != null && !builder.isIgnored()) {
                        writer.write("See <a style=\"color:#337ab7;text-decoration:underline;\" href=\"");
                        writer.write(req.getServletContext().getContextPath());
                        // This path must be hardcoded unless we find an easy way to reference the client-specific configuration
                        writer.write(REST_URL_PATTERN_BASE + "/#/invocation/");
                        writer.write("" + builder.getEntryPointIdx());
                        writer.write("\" target=\"_blank\">all bean invocations</a> within the HTTP request which rendered this page.");
                    }
                    writer.write("</div>");
                    writer.write(captured.substring(idx, captured.length()));
                    out.write(writer.toString());
                }
            } else {
                out.write(captured);
            }
        }
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

    private String initSnippetBase(ServletContext servletContext) {
        // Note that we have to use in-line CSS
        StringBuilder builder = new StringBuilder();
        builder.append("<!-- The following snippet was automatically added by Weld, see the documentation to disable this functionality -->");
        builder.append(
                "<div id=\"weld-dev-mode-info\" style=\"position: fixed !important;bottom:0;left:0;width:100%;background-color:#f8f8f8;border:2px solid silver;padding:10px;border-radius:2px;margin:0px;font-size:14px;font-family:sans-serif;color:black;\">");
        builder.append("<img alt=\"Weld logo\" style=\"vertical-align: middle;border-width:0px;\" src=\"");
        builder.append(servletContext.getContextPath());
        builder.append(REST_URL_PATTERN_BASE + "/client/weld_icon_32x.png\">");
        builder.append("&nbsp; Running on Weld <span style=\"color:gray\">");
        builder.append(Formats.getSimpleVersion());
        builder.append(
                "</span>. The development mode is <span style=\"color:white;background-color:#d62728;padding:6px;border-radius:4px;font-size:12px;\">ENABLED</span>. Inspect your application with <a style=\"color:#337ab7;text-decoration:underline;\" href=\"");
        builder.append(servletContext.getContextPath());
        builder.append(REST_URL_PATTERN_BASE);
        builder.append("\" target=\"_blank\">Probe Development Tool</a>.");
        builder.append(
                " <button style=\"float:right;background-color:#f8f8f8;border:1px solid silver; color:gray;border-radius:4px;padding:4px 10px 4px 10px;margin-left:2em;font-weight: bold;\" onclick=\"document.getElementById('weld-dev-mode-info').style.display='none';\">x</button>");
        return builder.toString();
    }

    private void processResourceRequest(HttpServletRequest req, HttpServletResponse resp, HttpMethod httpMethod, String[] resourcePathParts)
            throws IOException {
        Resource resource;
        if (resourcePathParts.length == 0) {
            resource = Resource.CLIENT_RESOURCE;
        } else {
            resource = matchResource(resourcePathParts);
            if (resource == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        ProbeLogger.LOG.resourceMatched(resource, req.getRequestURI());
        resource.handle(httpMethod, jsonDataProvider, resourcePathParts, req, resp);
    }

    private Resource matchResource(String[] resourcePathParts) {
        for (Resource resource : Resource.values()) {
            if (resource.matches(resourcePathParts)) {
                return resource;
            }
        }
        return null;
    }

    /**
     *
     * @return the array of resource path parts or <code>null</code> if the given URI does not represent a Probe resource
     */
    static String[] getResourcePathParts(String requestUri, String contextPath) {
        final String path = requestUri.substring(contextPath.length(), requestUri.length());
        if (path.startsWith(REST_URL_PATTERN_BASE)) {
            return Resource.splitPath(path.substring(REST_URL_PATTERN_BASE.length(), path.length()));
        }
        return null;
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
