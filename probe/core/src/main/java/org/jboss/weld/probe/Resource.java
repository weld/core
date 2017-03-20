/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.probe.Strings.APPLICATION_FONT_MS;
import static org.jboss.weld.probe.Strings.APPLICATION_FONT_SFNT;
import static org.jboss.weld.probe.Strings.APPLICATION_FONT_WOFF;
import static org.jboss.weld.probe.Strings.ENCODING_UTF8;
import static org.jboss.weld.probe.Strings.FILE_CLIENT_HTML;
import static org.jboss.weld.probe.Strings.FILTERS;
import static org.jboss.weld.probe.Strings.HTTP_HEADER_CACHE_CONTROL;
import static org.jboss.weld.probe.Strings.IMG_ICO;
import static org.jboss.weld.probe.Strings.IMG_PNG;
import static org.jboss.weld.probe.Strings.IMG_SVG;
import static org.jboss.weld.probe.Strings.PAGE;
import static org.jboss.weld.probe.Strings.PAGE_SIZE;
import static org.jboss.weld.probe.Strings.PARAM_TRANSIENT_DEPENDENCIES;
import static org.jboss.weld.probe.Strings.PARAM_TRANSIENT_DEPENDENTS;
import static org.jboss.weld.probe.Strings.PATH_META_INF_CLIENT;
import static org.jboss.weld.probe.Strings.REPRESENTATION;
import static org.jboss.weld.probe.Strings.RESOURCE_PARAM_END;
import static org.jboss.weld.probe.Strings.RESOURCE_PARAM_START;
import static org.jboss.weld.probe.Strings.SLASH;
import static org.jboss.weld.probe.Strings.SUFFIX_CSS;
import static org.jboss.weld.probe.Strings.SUFFIX_EOT;
import static org.jboss.weld.probe.Strings.SUFFIX_HTML;
import static org.jboss.weld.probe.Strings.SUFFIX_ICO;
import static org.jboss.weld.probe.Strings.SUFFIX_JS;
import static org.jboss.weld.probe.Strings.SUFFIX_OTF;
import static org.jboss.weld.probe.Strings.SUFFIX_PNG;
import static org.jboss.weld.probe.Strings.SUFFIX_SVG;
import static org.jboss.weld.probe.Strings.SUFFIX_TTF;
import static org.jboss.weld.probe.Strings.SUFFIX_WOFF;
import static org.jboss.weld.probe.Strings.TEXT_CSS;
import static org.jboss.weld.probe.Strings.TEXT_HTML;
import static org.jboss.weld.probe.Strings.TEXT_JAVASCRIPT;
import static org.jboss.weld.probe.Strings.TEXT_PLAIN;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Vetoed;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.weld.probe.Queries.Filters;

/**
 * REST API resources.
 *
 * @author Martin Kouba
 */
@Vetoed
enum Resource {

    /**
     * A deployment info.
     */
    DEPLOYMENT("/deployment", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            append(resp, jsonDataProvider.receiveDeployment());
        }
    }),
    /**
     * A collection of beans.
     */
    BEANS("/beans", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            append(resp, jsonDataProvider.receiveBeans(getPage(req), getPageSize(req), req.getParameter(FILTERS), req.getParameter(REPRESENTATION)));
        }
    }),
    /**
     * A single bean detail.
     */
    BEAN("/beans/{.+}", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            appendFound(resp, jsonDataProvider.receiveBean(resourcePathParts[1], Boolean.valueOf(req.getParameter(PARAM_TRANSIENT_DEPENDENCIES)),
                    Boolean.valueOf(req.getParameter(PARAM_TRANSIENT_DEPENDENTS))));
        }
    }),
    /**
     * A contextual instance of a bean. This is only supported for a limited set of scopes.
     */
    BEAN_INSTANCE("/beans/{.+}/instance", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            appendFound(resp, jsonDataProvider.receiveBeanInstance(resourcePathParts[1]));
        }
    }),
    /**
     * A collection of observers methods.
     */
    OBSERVERS("/observers", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            append(resp, jsonDataProvider.receiveObservers(getPage(req), getPageSize(req), req.getParameter(FILTERS), req.getParameter(REPRESENTATION)));
        }
    }),
    /**
     * A single observer.
     */
    OBSERVER("/observers/{.+}", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            appendFound(resp, jsonDataProvider.receiveObserver(resourcePathParts[1]));
        }
    }),
    /**
     * A collection of inspectable contexts.
     */
    CONTEXTS("/contexts", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            append(resp, jsonDataProvider.receiveContexts());
        }
    }),
    /**
     * A collection of contextual instances for the given inspectable context.
     */
    CONTEXT("/contexts/{[a-zA-Z_0]+}", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            appendFound(resp, jsonDataProvider.receiveContext(resourcePathParts[1]));
        }
    }),
    /**
     * A collection of invocation trees.
     */
    INVOCATIONS("/invocations", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            append(resp, jsonDataProvider.receiveInvocations(getPage(req), getPageSize(req), req.getParameter(FILTERS), req.getParameter(REPRESENTATION)));
        }

        @Override
        protected void delete(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            append(resp, jsonDataProvider.clearInvocations());
        }
    }),
    /**
     * A single invocation tree.
     */
    INVOCATION("/invocations/{.+}", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            appendFound(resp, jsonDataProvider.receiveInvocation(resourcePathParts[1]));
        }
    }),
    /**
     * The event bus
     */
    EVENTS("/events", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            append(resp, jsonDataProvider.receiveEvents(getPage(req), getPageSize(req), req.getParameter(FILTERS)));
        }

        @Override
        protected void delete(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            append(resp, jsonDataProvider.clearEvents());
        }
    }),
    MONITORING_STATS("/monitoring", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            append(resp, jsonDataProvider.receiveMonitoringStats());
        }
    }),
    AVAILABLE_BEANS("/availableBeans", new Handler() {
        @Override
        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            append(resp, jsonDataProvider.receiveAvailableBeans(getPage(req), getPageSize(req), req.getParameter(FILTERS), req.getParameter(REPRESENTATION)));
        }
    }),
    /**
     * A default HTML client resource.
     */
    CLIENT_RESOURCE("/client/{[a-zA-Z_0-9-]+\\.\\w+}", new Handler() {
        @Override
        protected void handle(HttpMethod method, JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req,
                HttpServletResponse resp) throws IOException {

            if (!HttpMethod.GET.equals(method)) {
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            String resourceName = PATH_META_INF_CLIENT + (resourcePathParts.length == 0 ? FILE_CLIENT_HTML : resourcePathParts[resourcePathParts.length - 1]);
            String contentType = detectContentType(resourceName);
            setHeaders(resp, contentType);

            if (isCachableContentType(contentType)) {
                // Set Cache-Control header - 24 hours
                resp.setHeader(HTTP_HEADER_CACHE_CONTROL, "max-age=86400");
            }

            if (isTextBasedContenType(contentType)) {
                String content = IOUtils.getResourceAsString(resourceName);
                if (content == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                content = content.replace("${contextPath}", req.getServletContext().getContextPath() + ProbeFilter.REST_URL_PATTERN_BASE + SLASH);
                resp.getWriter().append(content);
            } else {
                if (!IOUtils.writeResource(resourceName, resp.getOutputStream())) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        }
    }),
    EXPORT("/export", new Handler() {
        @Override
        protected void handle(HttpMethod method, JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req,
                HttpServletResponse resp) throws IOException {
            if (!HttpMethod.GET.equals(method)) {
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            setHeaders(resp, "application/zip");
            resp.setHeader("Content-disposition", "attachment; filename=\"weld-probe-export.zip\"");
            ServletOutputStream out = resp.getOutputStream();
            out.write(Exports.exportJsonData(jsonDataProvider));
            out.flush();
        }
    }),
    ;

    // --- Instance variables

    private final String[] parts;

    private final Handler handler;

    Resource(String path, Handler handler) {
        this.parts = splitPath(path);
        this.handler = handler;
    }

    protected void handle(HttpMethod method, JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handler.handle(method, jsonDataProvider, resourcePathParts, req, resp);
    }

    /**
     * @param resourcePathParts
     * @return <code>true</code> if the resource matches the given path, <code>false</code> otherwise
     */
    boolean matches(String[] resourcePathParts) {
        if (resourcePathParts.length != parts.length) {
            return false;
        }
        for (int i = 0; i < parts.length; i++) {
            if (isParam(parts[i])) {
                if (!resourcePathParts[i].matches(parts[i].substring(1, parts[i].length() - 1))) {
                    return false;
                }
            } else if (!parts[i].equals(resourcePathParts[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the parts of the path
     */
    String[] getParts() {
        return parts;
    }

    static boolean isParam(String part) {
        return part.startsWith(RESOURCE_PARAM_START) && part.endsWith(RESOURCE_PARAM_END);
    }

    static String[] splitPath(String path) {
        List<String> parts = new ArrayList<String>();
        StringBuilder builder = null;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '/') {
                if (builder != null) {
                    parts.add(builder.toString());
                    builder = null;
                }
            } else {
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(c);
            }
        }
        if (builder != null) {
            parts.add(builder.toString());
        }
        return parts.toArray(new String[parts.size()]);
    }

    static String detectContentType(String resourceName) {
        if (resourceName.endsWith(SUFFIX_HTML)) {
            return TEXT_HTML;
        } else if (resourceName.endsWith(SUFFIX_CSS)) {
            return TEXT_CSS;
        } else if (resourceName.endsWith(SUFFIX_JS)) {
            return TEXT_JAVASCRIPT;
        } else if (resourceName.endsWith(SUFFIX_PNG)) {
            return IMG_PNG;
        } else if (resourceName.endsWith(SUFFIX_TTF) || resourceName.endsWith(SUFFIX_OTF)) {
            return APPLICATION_FONT_SFNT;
        } else if (resourceName.endsWith(SUFFIX_EOT)) {
            return APPLICATION_FONT_MS;
        } else if (resourceName.endsWith(SUFFIX_WOFF)) {
            return APPLICATION_FONT_WOFF;
        } else if (resourceName.endsWith(SUFFIX_SVG)) {
            return IMG_SVG;
        } else if (resourceName.endsWith(SUFFIX_ICO)) {
            return IMG_ICO;
        } else {
            return TEXT_PLAIN;
        }
    }

    static boolean isCachableContentType(String contentType) {
        return TEXT_CSS.equals(contentType) || TEXT_JAVASCRIPT.equals(contentType) || IMG_ICO.equals(contentType) || IMG_PNG.equals(contentType)
                || IMG_SVG.equals(contentType);
    }

    static boolean isTextBasedContenType(String contentType) {
        return !(IMG_PNG.equals(contentType) || IMG_ICO.equals(contentType) || APPLICATION_FONT_SFNT.equals(contentType)
                || APPLICATION_FONT_WOFF.equals(contentType) || APPLICATION_FONT_MS.equals(contentType));
    }

    abstract static class Handler implements Serializable {
        private static final long serialVersionUID = 5253937931990206305L;

        protected void handle(HttpMethod method, JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req,
                HttpServletResponse resp) throws IOException {
            setHeaders(resp, getContentType());
            switch (method) {
                case GET:
                    get(jsonDataProvider, resourcePathParts, req, resp);
                    break;
                case POST:
                    post(jsonDataProvider, resourcePathParts, req, resp);
                    break;
                case DELETE:
                    delete(jsonDataProvider, resourcePathParts, req, resp);
                    break;
                case OPTIONS:
                    options(jsonDataProvider, resourcePathParts, req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        }

        protected void get(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        protected void post(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        protected void delete(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        protected void options(JsonDataProvider jsonDataProvider, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            setCorsHeaders(resp);
        }

        protected void append(HttpServletResponse resp, String content) throws IOException {
            resp.getWriter().append(content);
        }

        protected void appendFound(HttpServletResponse resp, String content) throws IOException {
            if (content != null) {
                resp.getWriter().append(content);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        protected int getPage(HttpServletRequest req) {
            String pageParam = req.getParameter(PAGE);
            if (pageParam == null) {
                return 1;
            }
            try {
                return Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                return 1;
            }
        }

        protected int getPageSize(HttpServletRequest req) {
            String pageSizeParam = req.getParameter(PAGE_SIZE);
            if (pageSizeParam == null) {
                return Queries.DEFAULT_PAGE_SIZE;
            } else {
                try {
                    int result = Integer.parseInt(pageSizeParam);
                    return result > 0 ? result : 0;
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }

        protected <E, T extends Filters<E>> T initFilters(HttpServletRequest req, T filters) {
            return Queries.initFilters(req.getParameter(FILTERS), filters);
        }

        protected String getContentType() {
            return Strings.APPLICATION_JSON;
        }

        static void setHeaders(HttpServletResponse resp, String contentType) {
            resp.setCharacterEncoding(ENCODING_UTF8);
            resp.setContentType(contentType);
            setCorsHeaders(resp);
        }

        static void setCorsHeaders(HttpServletResponse resp) {
            // Support cross-site HTTP requests - we want to support external HTML5 clients
            // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
        }

    }

    static enum HttpMethod {

        GET, POST, DELETE, OPTIONS;

        static HttpMethod from(String method) {
            for (HttpMethod httpMethod : values()) {
                if (httpMethod.toString().equalsIgnoreCase(method)) {
                    return httpMethod;
                }
            }
            return null;
        }

    }

    static enum Representation {

        SIMPLE, BASIC, FULL,

        ;

        static Representation from(String value) {
            for (Representation representation : values()) {
                if (representation.toString().equalsIgnoreCase(value)) {
                    return representation;
                }
            }
            return null;
        }

    }

}