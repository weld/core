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

package org.jboss.weld.osgi.examples.web.fwk;

import java.io.IOException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.http.HttpContext;

public class OSGiHttpContext implements HttpContext {

    private final ClassLoader loader;

    public OSGiHttpContext(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        return true; // TODO support pluggable security
    }

    @Override
    public URL getResource(String name) {
        return loader.getResource(name.replace("tmp/", ""));
    }

    @Override
    public String getMimeType(String name) {
        if (name.endsWith(".css")) {
            return "text/css";
        }
        return "*"; // TODO map with real types
    }
}
