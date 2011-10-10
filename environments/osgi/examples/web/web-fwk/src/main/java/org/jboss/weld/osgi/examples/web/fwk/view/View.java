/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.osgi.examples.web.fwk.view;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 *
 * @author Mathieu ANCELIN
 */
public class View extends Renderable {

    private static final String TYPE = MediaType.TEXT_HTML;
    private final String viewName;
    private final Map<String, Object> context;
    private final ClassLoader loader;

    public View(String viewName, Class<?> from) {
        this.contentType = TYPE;
        this.viewName = viewName;
        this.context = new HashMap<String, Object>();
        this.loader = from.getClassLoader();
    }

    public View(String viewName, Map<String, Object> context, Class<?> from) {
        this.contentType = TYPE;
        this.viewName = viewName;
        this.context = context;
        this.loader = from.getClassLoader();
    }

    public View param(String name, Object value) {
        this.context.put(name, value);
        return this;
    }

    @Override
    public Response render() {
        try {
            String renderText = "";
            ResponseBuilder builder = Response.ok(renderText, TYPE);
            return builder.build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void render(ServletResponse resp) {
        try {
            resp.setContentType("text/html");
            PrintWriter w = resp.getWriter();
            w.println(render());
            w.close();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
