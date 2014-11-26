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

import static org.jboss.weld.probe.ResourcePath.BEAN;
import static org.jboss.weld.probe.ResourcePath.BEANS;
import static org.jboss.weld.probe.ResourcePath.BEAN_INSTANCE;
import static org.jboss.weld.probe.ResourcePath.CLIENT_RESOURCE;
import static org.jboss.weld.probe.ResourcePath.CONTEXTS;
import static org.jboss.weld.probe.ResourcePath.DEPLOYMENT;
import static org.jboss.weld.probe.ResourcePath.INVOCATION;
import static org.jboss.weld.probe.ResourcePath.INVOCATIONS;
import static org.jboss.weld.probe.ResourcePath.OBSERVER;
import static org.jboss.weld.probe.ResourcePath.OBSERVERS;
import static org.jboss.weld.probe.ResourcePath.ROOT;
import static org.jboss.weld.probe.ResourcePath.detectContentType;
import static org.jboss.weld.probe.Strings.ENCODING_UTF8;
import static org.jboss.weld.probe.Strings.IMG_PNG;
import static org.jboss.weld.probe.Strings.REMOVED_INVOCATIONS;
import static org.jboss.weld.probe.Strings.SLASH;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Queries.BeanFilters;
import org.jboss.weld.probe.Queries.Filters;
import org.jboss.weld.probe.Queries.InvocationsFilters;
import org.jboss.weld.probe.Queries.ObserverFilters;

/**
 * A simple Probe REST API implementation.
 *
 * <p>
 * An integrator is required to register this servlet if appropriate.
 * </p>
 *
 * @author Martin Kouba
 */
public class ProbeServlet extends HttpServlet {

    public static final String DEFAULT_URL_PATTERN = "/weld-probe/*";

    private static final String PARAM_TRANSIENT_DEPENDENCIES = "transientDependencies";

    private static final String PARAM_TRANSIENT_DEPENDENTS = "transientDependents";

    private static final String PARAM_PAGE = "page";

    private static final String PARAM_FILTERS = "filters";

    private static final String FILE_CLIENT_HTML = "probe.html";

    private static final int DEFAULT_BUFFER = 1024 * 8;

    private static final long serialVersionUID = -881168492978480911L;

    @Inject
    private BeanManagerImpl beanManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Probe probe = getProbe();
        ResourcePath resourcePath;
        String[] pathInfoParts = getPathInfoParts(req);

        if (pathInfoParts == null) {
            resourcePath = ResourcePath.ROOT;
        } else {
            resourcePath = match(pathInfoParts);
            if (resourcePath == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        ProbeLogger.LOG.resourcePathMatched(resourcePath, req.getPathInfo());

        if (ROOT.equals(resourcePath) || CLIENT_RESOURCE.equals(resourcePath)) {

            // Root resource - a default HTML client
            String resourceName;
            if (pathInfoParts == null) {
                resourceName = FILE_CLIENT_HTML;
            } else {
                resourceName = ROOT.equals(resourcePath) ? pathInfoParts[0] : pathInfoParts[1];
            }

            String contentType = detectContentType(resourceName);

            if (IMG_PNG.equals(contentType)) {
                if (!writeResource(resourceName, resp.getOutputStream())) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                // All other content types are text-based
                String content = getResourceAsString(resourceName);
                if (content == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
                content = content.replace("${contextPath}", req.getContextPath() + req.getServletPath() + SLASH);
                resp.getWriter().append(content);
            }
            setHeaders(resp, contentType);

            // Originally, some API info was provided by the root resource
            // resp.getWriter().append(JsonObjects.createRootJson());

        } else {

            if (DEPLOYMENT.equals(resourcePath)) {
                // Deployment info
                resp.getWriter().append(JsonObjects.createDeploymentJson(beanManager));

            } else if (BEANS.equals(resourcePath)) {

                // Bean list
                resp.getWriter().append(
                        JsonObjects.createBeansJson(Queries.find(probe.getBeans(), getPage(req), initFilters(req, new BeanFilters(probe))), probe));

            } else if (BEAN.equals(resourcePath)) {

                // Bean detail
                Bean<?> bean = probe.getBean(pathInfoParts[1]);
                if (bean != null) {
                    resp.getWriter().append(
                            JsonObjects.createFullBeanJson(bean, Boolean.valueOf(req.getParameter(PARAM_TRANSIENT_DEPENDENCIES)),
                                    Boolean.valueOf(req.getParameter(PARAM_TRANSIENT_DEPENDENTS)), probe));
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }

            } else if (BEAN_INSTANCE.equals(resourcePath)) {

                // Bean contextual instance
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

            } else if (OBSERVERS.equals(resourcePath)) {

                // Observer methods
                resp.getWriter().append(
                        JsonObjects.createObserversJson(Queries.find(probe.getObservers(), getPage(req), initFilters(req, new ObserverFilters(probe))), probe));

            } else if (OBSERVER.equals(resourcePath)) {

                // Observer method detail
                ObserverMethod<?> observer = probe.getObserver(pathInfoParts[1]);
                if (observer != null) {
                    resp.getWriter().append(JsonObjects.createFullObserverJson(observer, probe));
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }

            } else if (CONTEXTS.equals(resourcePath)) {

                // Inspectable contexts
                resp.getWriter().append(JsonObjects.createContextsJson(beanManager, probe));

            } else if (INVOCATIONS.equals(resourcePath)) {

                // Invocations
                resp.getWriter().append(
                        JsonObjects.createInvocationsJson(Queries.find(probe.getInvocations(), getPage(req), initFilters(req, new InvocationsFilters(probe))),
                                probe));

            } else if (INVOCATION.equals(resourcePath)) {

                // Invocation tree detail
                Invocation entryPoint = probe.getInvocation(pathInfoParts[1]);
                if (entryPoint != null) {
                    resp.getWriter().append(JsonObjects.createFullInvocationJson(entryPoint, probe).build());
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }

            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            setHeaders(resp, resourcePath);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Probe probe = getProbe();
        ResourcePath resourcePath;
        String[] pathInfoParts = getPathInfoParts(req);

        if (pathInfoParts == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            resourcePath = match(pathInfoParts);
            if (resourcePath == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        ProbeLogger.LOG.resourcePathMatched(resourcePath, req.getPathInfo());

        if (INVOCATIONS.equals(resourcePath)) {

            // Clear invocations
            resp.getWriter().append(Json.newObjectBuilder().add(REMOVED_INVOCATIONS, probe.clearInvocations()).build());

        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        setHeaders(resp, resourcePath);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
    }

    private void setHeaders(HttpServletResponse resp, ResourcePath resourcePath) {
        setHeaders(resp, resourcePath.getDefaultContentType());
    }

    private void setHeaders(HttpServletResponse resp, String contentType) {
        resp.setCharacterEncoding(ENCODING_UTF8);
        resp.setContentType(contentType);
        setCorsHeaders(resp);
    }

    private void setCorsHeaders(HttpServletResponse resp) {
        // Support cross-site HTTP requests
        // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
    }

    private ResourcePath match(String[] pathInfoParts) {
        for (ResourcePath resource : ResourcePath.values()) {
            if (resource.matches(pathInfoParts)) {
                return resource;
            }
        }
        return null;
    }

    private String[] getPathInfoParts(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        return pathInfo != null ? ResourcePath.splitPath(pathInfo) : null;
    }

    private Probe getProbe() {
        if (beanManager == null) {
            throw new IllegalStateException("Could not inject the BeanManagerImpl");
        }
        Probe probe = beanManager.getServices().get(Probe.class);
        if (probe == null) {
            throw new IllegalStateException("Unable to find the Probe service");
        }
        return probe;
    }

    private int getPage(HttpServletRequest req) {
        String pageParam = req.getParameter(PARAM_PAGE);
        if (pageParam == null) {
            return 1;
        }
        try {
            return Integer.valueOf(pageParam);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String getResourceAsString(String resourceName) {
        StringWriter writer = new StringWriter();
        BufferedReader reader = null;
        try {
            InputStream in = ProbeServlet.class.getResourceAsStream("/META-INF/client/" + resourceName);
            if (in == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(in, ENCODING_UTF8));
            final char[] buffer = new char[DEFAULT_BUFFER];
            int n = 0;
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }
            writer.flush();
            return writer.toString();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private boolean writeResource(String resourceName, OutputStream out) {
        InputStream in = ProbeServlet.class.getResourceAsStream("/META-INF/client/" + resourceName);
        if (in == null) {
            return false;
        }
        try {
            final byte[] buffer = new byte[DEFAULT_BUFFER];
            int n = 0;
            while (-1 != (n = in.read(buffer))) {
                out.write(buffer, 0, n);
            }
            out.flush();
            return true;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    static <E, T extends Filters<E>> T initFilters(HttpServletRequest req, T filters) {
        String filtersParam = req.getParameter(PARAM_FILTERS);
        if (filtersParam == null || filtersParam.trim().length() == 0) {
            return null;
        }
        filters.processFilters(filtersParam);
        return filters;
    }

}
