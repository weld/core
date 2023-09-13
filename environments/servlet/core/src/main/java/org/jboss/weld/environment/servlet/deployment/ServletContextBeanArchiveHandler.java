/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.servlet.deployment;

import static org.jboss.weld.environment.util.URLUtils.PROTOCOL_WAR_PART;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jakarta.servlet.ServletContext;

import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;
import org.jboss.weld.environment.servlet.logging.WeldServletLogger;
import org.jboss.weld.environment.util.Files;

/**
 * Handles the paths to resources within a web application if a WAR archive is not extracted to the file system.
 * <p>
 * For {@code WEB-INF/classes}, {@link ServletContext#getResourcePaths(String)} is used. For libraries, only {@code war}
 * protocol is supported.
 *
 * @author Martin Kouba
 * @author Thomas Meyer
 */
public class ServletContextBeanArchiveHandler implements BeanArchiveHandler {

    protected static final String SLASH = "/";

    protected static final String DOT = ".";

    protected final ServletContext servletContext;

    /**
     * @param servletContext
     */
    public ServletContextBeanArchiveHandler(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public BeanArchiveBuilder handle(String path) {
        if (path.equals(WebAppBeanArchiveScanner.WEB_INF_CLASSES)) {
            BeanArchiveBuilder builder = new BeanArchiveBuilder();
            handleResourcePath(path, path, builder);
            return builder;
        } else if (path.startsWith(PROTOCOL_WAR_PART)) {
            try {
                URL url = new URL(path);
                InputStream in = url.openStream();
                if (in != null) {
                    BeanArchiveBuilder builder = new BeanArchiveBuilder();
                    handleLibrary(url, in, builder);
                    return builder;
                }
            } catch (IOException e) {
                WeldServletLogger.LOG.cannotHandleLibrary(path, e);
            }
        }
        return null;
    }

    protected void add(String rootPath, String subpath, BeanArchiveBuilder builder) {
        // Class file
        String className = toClassName(rootPath, subpath);
        builder.addClass(className);
        WeldServletLogger.LOG.tracev("Class discovered: {0}", className);
    }

    private void handleResourcePath(String rootPath, String resourcePath, BeanArchiveBuilder builder) {
        WeldServletLogger.LOG.debugv("Handle resource path: {0}", resourcePath);
        Set<String> subpaths = servletContext.getResourcePaths(resourcePath);
        if (subpaths != null && !subpaths.isEmpty()) {
            for (String subpath : subpaths) {
                if (subpath.endsWith(SLASH)) {
                    // Paths indicating subdirectory end with a '/'
                    handleResourcePath(rootPath, subpath, builder);
                } else if (subpath.endsWith(Files.CLASS_FILE_EXTENSION)) {
                    add(rootPath, subpath, builder);
                }
            }
        }
    }

    private void handleLibrary(URL url, InputStream in, BeanArchiveBuilder builder) throws IOException {
        WeldServletLogger.LOG.debugv("Handle library: {0}", url);
        try (ZipInputStream zip = new ZipInputStream(in)) {
            ZipEntry entry = null;
            while ((entry = zip.getNextEntry()) != null) {
                if (Files.isClass(entry.getName())) {
                    builder.addClass(Files.filenameToClassname(entry.getName()));
                }
            }
        }
    }

    private String toClassName(String rootPath, String resourcePath) {
        // Remove WEB-INF/classes part, suffix and replace slashes with dots
        return resourcePath.substring(rootPath.length() + 1, resourcePath.lastIndexOf(Files.CLASS_FILE_EXTENSION))
                .replace(SLASH, DOT);
    }

}
