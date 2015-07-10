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
import static org.jboss.weld.probe.Strings.REMOVED_INVOCATIONS;
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
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Queries.BeanFilters;
import org.jboss.weld.probe.Queries.EventsFilters;
import org.jboss.weld.probe.Queries.Filters;
import org.jboss.weld.probe.Queries.InvocationsFilters;
import org.jboss.weld.probe.Queries.ObserverFilters;

/**
 * Enum of resources.
 *
 * @author Martin Kouba
 */
enum Resource {

    /**
     * A deployment info.
     */
    DEPLOYMENT("/deployment", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(JsonObjects.createDeploymentJson(beanManager, probe));
        }
    }),
    /**
     * A collection of beans.
     */
    BEANS("/beans", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            Representation representation = Representation.from(req.getParameter(REPRESENTATION));
            if (representation == null) {
                representation = Representation.BASIC;
            }
            resp.getWriter().append(
                    JsonObjects.createBeansJson(Queries.find(probe.getBeans(), getPage(req), getPageSize(req), initFilters(req, new BeanFilters(probe))),
                            probe, beanManager, representation));
        }
    }),
    /**
     * A single bean detail.
     */
    BEAN("/beans/{.+}", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            Bean<?> bean = probe.getBean(resourcePathParts[1]);
            if (bean != null) {
                resp.getWriter().append(
                        JsonObjects.createFullBeanJson(bean, Boolean.valueOf(req.getParameter(PARAM_TRANSIENT_DEPENDENCIES)),
                                Boolean.valueOf(req.getParameter(PARAM_TRANSIENT_DEPENDENTS)), beanManager, probe));
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }),
    /**
     * A contextual instance of a bean. This is only supported for a limited set of scopes.
     */
    BEAN_INSTANCE("/beans/{.+}/instance", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            Bean<?> bean = probe.getBean(resourcePathParts[1]);
            if (bean != null && Components.isInspectableScope(bean.getScope())) {
                Object instance = Components.findContextualInstance(bean, beanManager);
                if (instance != null) {
                    resp.getWriter().append(JsonObjects.createContextualInstanceJson(bean, instance, probe));
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }),
    /**
     * A collection of observers methods.
     */
    OBSERVERS("/observers", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(
                    JsonObjects.createObserversJson(
                            Queries.find(probe.getObservers(), getPage(req), getPageSize(req), initFilters(req, new ObserverFilters(probe))), probe));
        }
    }),
    /**
     * A single observer.
     */
    OBSERVER("/observers/{.+}", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            ObserverMethod<?> observer = probe.getObserver(resourcePathParts[1]);
            if (observer != null) {
                resp.getWriter().append(JsonObjects.createFullObserverJson(observer, probe));
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }),
    /**
     * A collection of inspectable contexts.
     */
    CONTEXTS("/contexts", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(JsonObjects.createContextsJson(beanManager, probe).build());
        }
    }),
    /**
     * A collection of inspectable contexts.
     */
    CONTEXT("/contexts/{[a-zA-Z_0]+}", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            final String id = resourcePathParts[1];
            final Class<? extends Annotation> scope = Components.INSPECTABLE_SCOPES.get(id);
            if (scope != null) {
                resp.getWriter().append(JsonObjects.createContextJson(id, scope, beanManager, probe, req).build());
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        }
    }),
    /**
     * A collection of invocation trees.
     */
    INVOCATIONS("/invocations", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(
                    JsonObjects.createInvocationsJson(
                            Queries.find(probe.getInvocations(), getPage(req), getPageSize(req), initFilters(req, new InvocationsFilters(probe))), probe));
        }

        @Override
        protected void handleDelete(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(Json.objectBuilder().add(REMOVED_INVOCATIONS, probe.clearInvocations()).build());
        }
    }),
    /**
     * A single invocation tree.
     */
    INVOCATION("/invocations/{.+}", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            Invocation entryPoint = probe.getInvocation(resourcePathParts[1]);
            if (entryPoint != null) {
                resp.getWriter().append(JsonObjects.createFullInvocationJson(entryPoint, probe).build());
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }),
    /**
     * The event bus
     */
    EVENTS("/events", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(
                    JsonObjects.createEventsJson(Queries.find(probe.getEvents(), getPage(req), getPageSize(req), initFilters(req, new EventsFilters(probe))),
                            probe));
        }

        @Override
        protected void handleDelete(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(Json.objectBuilder().add("removedEvents", probe.clearEvents()).build());
        }
    }),
    /**
     * A default HTML client resource.
     */
    CLIENT_RESOURCE("/client/{[a-zA-Z_0-9-]+\\.\\w+}", new Handler() {
        @Override
        protected void handle(BeanManagerImpl beanManager, Probe probe, HttpMethod method, String[] resourcePathParts, HttpServletRequest req,
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
    }), ;

    // --- Instance variables

    private final String[] parts;

    private final Handler handler;

    Resource(String path, Handler handler) {
        this.parts = splitPath(path);
        this.handler = handler;
    }

    protected void handle(BeanManagerImpl beanManager, Probe probe, HttpMethod method, String[] resourcePathParts, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        handler.handle(beanManager, probe, method, resourcePathParts, req, resp);
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

    abstract static class Handler {

        protected void handle(BeanManagerImpl beanManager, Probe probe, HttpMethod method, String[] resourcePathParts, HttpServletRequest req,
                HttpServletResponse resp) throws IOException {
            setHeaders(resp, getContentType());
            switch (method) {
                case GET:
                    handleGet(beanManager, probe, resourcePathParts, req, resp);
                    break;
                case POST:
                    handlePost(beanManager, probe, resourcePathParts, req, resp);
                    break;
                case DELETE:
                    handleDelete(beanManager, probe, resourcePathParts, req, resp);
                    break;
                case OPTIONS:
                    handleOptions(beanManager, probe, resourcePathParts, req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        }

        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        protected void handlePost(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        protected void handleDelete(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        protected void handleOptions(BeanManagerImpl beanManager, Probe probe, String[] resourcePathParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            setCorsHeaders(resp);
        }

        protected int getPage(HttpServletRequest req) {
            String pageParam = req.getParameter(PAGE);
            if (pageParam == null) {
                return 1;
            }
            try {
                return Integer.valueOf(pageParam);
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
                    int result = Integer.valueOf(pageSizeParam);
                    return result > 0 ? result : 0;
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }

        protected <E, T extends Filters<E>> T initFilters(HttpServletRequest req, T filters) {
            String filtersParam = req.getParameter(FILTERS);
            if (filtersParam == null || filtersParam.trim().length() == 0) {
                return null;
            }
            filters.processFilters(filtersParam);
            return filters;
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