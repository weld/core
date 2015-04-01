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

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Resource.HttpMethod;

/**
 * A simple Probe REST API implementation.
 *
 * <p>
 * An integrator is required to register this servlet if appropriate. This servlet should only be mapped to a single URL pattern of value
 * {@value #DEFAULT_URL_PATTERN}.
 * </p>
 *
 * @author Martin Kouba
 */
public class ProbeServlet extends HttpServlet {

    static final String URL_PATTERN_BASE = "/weld-probe";

    public static final String DEFAULT_URL_PATTERN = URL_PATTERN_BASE + "/*";

    private static final long serialVersionUID = -881168492978480911L;

    @Inject
    private BeanManagerImpl beanManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, HttpMethod.GET);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, HttpMethod.DELETE);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Resource.Handler.setCorsHeaders(resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp, HttpMethod httpMethod) throws IOException {

        Probe probe = getProbe();
        String[] pathInfoParts = getPathInfoParts(req);
        Resource resource;

        if (pathInfoParts == null) {
            resource = Resource.CLIENT_RESOURCE;
        } else {
            resource = matchResource(pathInfoParts);
            if (resource == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        ProbeLogger.LOG.resourceMatched(resource, req.getPathInfo());
        resource.handle(beanManager, probe, httpMethod, pathInfoParts, req, resp);
    }

    private Resource matchResource(String[] pathInfoParts) {
        for (Resource resource : Resource.values()) {
            if (resource.matches(pathInfoParts)) {
                return resource;
            }
        }
        return null;
    }

    private String[] getPathInfoParts(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        return pathInfo != null ? Resource.splitPath(pathInfo) : null;
    }

    private Probe getProbe() {
        if (beanManager == null) {
            throw ProbeLogger.LOG.probeServletUnableToOperate(BeanManagerImpl.class);
        }
        Probe probe = beanManager.getServices().get(Probe.class);
        if (probe == null) {
            throw ProbeLogger.LOG.probeServletUnableToOperate(Probe.class);
        }
        return probe;
    }

}
