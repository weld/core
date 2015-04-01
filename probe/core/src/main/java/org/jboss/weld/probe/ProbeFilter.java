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
import org.jboss.weld.util.reflection.Formats;

/**
 * This servlet filter enables clippy-like support.
 *
 * <p>
 * An integrator is required to register this extension if appropriate.
 * </p>
 *
 * @see ConfigurationKey#PROBE_CLIPPY_SUPPORT
 * @author Martin Kouba
 */
public class ProbeFilter implements Filter {

    @Inject
    private BeanManagerImpl beanManager;

    // It shouldn't be necessary to make this field volatile - see also javax.servlet.GenericServlet.config
    private String clippy;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (beanManager.getServices().get(WeldConfiguration.class).getBooleanProperty(ConfigurationKey.PROBE_CLIPPY_SUPPORT)) {
            StringBuilder builder = new StringBuilder();
            builder.append("<!-- The following snippet was automatically added by Weld, see the documentation to disable this functionality -->");
            builder.append("<div id=\"weld-dev-mode-info\" style=\"width:auto;background-color:#f8f8f8;border:2px solid silver;padding:10px;border-radius:5px;margin:50px 5px 5px 5px;font-size:16px;font-family:sans-serif;color:black;\">");
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
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (clippy == null || httpServletRequest.getServletPath().startsWith(ProbeServlet.URL_PATTERN_BASE)) {
            chain.doFilter(request, response);
        } else {

            ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
            chain.doFilter(request, responseWrapper);

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

}
