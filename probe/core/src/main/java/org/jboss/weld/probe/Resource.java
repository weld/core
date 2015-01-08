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

import static org.jboss.weld.probe.Strings.ENCODING_UTF8;
import static org.jboss.weld.probe.Strings.FILE_CLIENT_HTML;
import static org.jboss.weld.probe.Strings.FILTERS;
import static org.jboss.weld.probe.Strings.IMG_PNG;
import static org.jboss.weld.probe.Strings.PAGE;
import static org.jboss.weld.probe.Strings.PARAM_TRANSIENT_DEPENDENCIES;
import static org.jboss.weld.probe.Strings.PARAM_TRANSIENT_DEPENDENTS;
import static org.jboss.weld.probe.Strings.PATH_META_INF_CLIENT;
import static org.jboss.weld.probe.Strings.REMOVED_INVOCATIONS;
import static org.jboss.weld.probe.Strings.RESOURCE_PARAM_END;
import static org.jboss.weld.probe.Strings.RESOURCE_PARAM_START;
import static org.jboss.weld.probe.Strings.SLASH;
import static org.jboss.weld.probe.Strings.SUFFIX_CSS;
import static org.jboss.weld.probe.Strings.SUFFIX_HTML;
import static org.jboss.weld.probe.Strings.SUFFIX_JS;
import static org.jboss.weld.probe.Strings.SUFFIX_PNG;
import static org.jboss.weld.probe.Strings.TEXT_CSS;
import static org.jboss.weld.probe.Strings.TEXT_HTML;
import static org.jboss.weld.probe.Strings.TEXT_JAVASCRIPT;
import static org.jboss.weld.probe.Strings.TEXT_PLAIN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Queries.BeanFilters;
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
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(JsonObjects.createDeploymentJson(beanManager));
        }
    }),
    /**
     * A collection of beans.
     */
    BEANS("/beans", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(JsonObjects.createBeansJson(Queries.find(probe.getBeans(), getPage(req), initFilters(req, new BeanFilters(probe))), probe));
        }
    }),
    /**
     * A single bean detail.
     */
    BEAN("/beans/{.+}", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            Bean<?> bean = probe.getBean(pathInfoParts[1]);
            if (bean != null) {
                resp.getWriter().append(
                        JsonObjects.createFullBeanJson(bean, Boolean.valueOf(req.getParameter(PARAM_TRANSIENT_DEPENDENCIES)),
                                Boolean.valueOf(req.getParameter(PARAM_TRANSIENT_DEPENDENTS)), probe));
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
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            Bean<?> bean = probe.getBean(pathInfoParts[1]);
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
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(
                    JsonObjects.createObserversJson(Queries.find(probe.getObservers(), getPage(req), initFilters(req, new ObserverFilters(probe))), probe));
        }
    }),
    /**
     * A single observer.
     */
    OBSERVER("/observers/{.+}", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            ObserverMethod<?> observer = probe.getObserver(pathInfoParts[1]);
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
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(JsonObjects.createContextsJson(beanManager, probe));
        }
    }),
    /**
     * A collection of invocation trees.
     */
    INVOCATIONS("/invocations", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(
                    JsonObjects.createInvocationsJson(Queries.find(probe.getInvocations(), getPage(req), initFilters(req, new InvocationsFilters(probe))),
                            probe));
        }

        @Override
        protected void handleDelete(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.getWriter().append(Json.newObjectBuilder().add(REMOVED_INVOCATIONS, probe.clearInvocations()).build());
        }
    }),
    /**
     * A single invocation tree.
     */
    INVOCATION("/invocations/{.+}", new Handler() {
        @Override
        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            Invocation entryPoint = probe.getInvocation(pathInfoParts[1]);
            if (entryPoint != null) {
                resp.getWriter().append(JsonObjects.createFullInvocationJson(entryPoint, probe).build());
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }),
    /**
     * A default HTML client resource.
     */
    CLIENT_RESOURCE("/client/{\\w+\\.\\w+}", new Handler() {
        @Override
        protected void handle(BeanManagerImpl beanManager, Probe probe, HttpMethod method, String[] pathInfoParts, HttpServletRequest req,
                HttpServletResponse resp) throws IOException {

            if (!HttpMethod.GET.equals(method)) {
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            String resourceName = PATH_META_INF_CLIENT + (pathInfoParts == null ? FILE_CLIENT_HTML : pathInfoParts[pathInfoParts.length - 1]);
            String contentType = detectContentType(resourceName);
            setHeaders(resp, contentType);

            if (IMG_PNG.equals(contentType)) {
                if (!IOUtils.writeResource(resourceName, resp.getOutputStream())) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                // All other content types are text-based
                String content = IOUtils.getResourceAsString(resourceName);
                if (content == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
                content = content.replace("${contextPath}", req.getContextPath() + req.getServletPath() + SLASH);
                resp.getWriter().append(content);
            }
        }
    }), ;

    // --- Instance variables

    private final String[] parts;

    private final String defaultContentType;

    private final Handler handler;

    Resource(String path, Handler handler) {
        this(path, handler, Strings.APPLICATION_JSON);
    }

    Resource(String path, Handler handler, String contentType) {
        this.parts = splitPath(path);
        this.handler = handler;
        this.defaultContentType = contentType;
    }

    protected void handle(BeanManagerImpl beanManager, Probe probe, HttpMethod method, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handler.handle(beanManager, probe, method, pathInfoParts, req, resp);
    }

    /**
     *
     * @param pathInfo
     * @return <code>true</code> if the resource matches the given path, <code>false</code> otherwise
     */
    boolean matches(String[] pathInfo) {
        if (pathInfo.length != parts.length) {
            return false;
        }
        for (int i = 0; i < parts.length; i++) {
            if (isParam(parts[i])) {
                if (!pathInfo[i].matches(parts[i].substring(1, parts[i].length() - 1))) {
                    return false;
                }
            } else if (!parts[i].equals(pathInfo[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isParam(String part) {
        return part.startsWith(RESOURCE_PARAM_START) && part.endsWith(RESOURCE_PARAM_END);
    }

    /**
     *
     * @return the parts of the path
     */
    String[] getParts() {
        return parts;
    }

    /**
     *
     * @return
     */
    String getDefaultContentType() {
        return defaultContentType;
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
        } else {
            return TEXT_PLAIN;
        }
    }

    abstract static class Handler {

        protected void handle(BeanManagerImpl beanManager, Probe probe, HttpMethod method, String[] pathInfoParts, HttpServletRequest req,
                HttpServletResponse resp) throws IOException {
            setHeaders(resp, getContentType());
            switch (method) {
                case GET:
                    handleGet(beanManager, probe, pathInfoParts, req, resp);
                    break;
                case POST:
                    handlePost(beanManager, probe, pathInfoParts, req, resp);
                    break;
                case DELETE:
                    handleDelete(beanManager, probe, pathInfoParts, req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        }

        protected void handleGet(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        protected void handlePost(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        protected void handleDelete(BeanManagerImpl beanManager, Probe probe, String[] pathInfoParts, HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
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
            // Support cross-site HTTP requests
            // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
        }

    }

    static enum HttpMethod {

        GET, POST, DELETE, OPTIONS;

    }

}