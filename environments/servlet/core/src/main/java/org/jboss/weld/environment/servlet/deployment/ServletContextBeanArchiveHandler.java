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

import java.util.Set;

import javax.servlet.ServletContext;

import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;
import org.jboss.weld.environment.servlet.logging.WeldServletLogger;
import org.jboss.weld.environment.util.Files;

/**
 * Handles the paths to resources within a web application. It's used if a WAR archive is not extracted to the file system.
 *
 * @author Martin Kouba
 */
public class ServletContextBeanArchiveHandler implements BeanArchiveHandler {

    private static final String SLASH = "/";

    private static final String DOT = ".";

    private final ServletContext servletContext;

    /**
     *
     * @param servletContext
     */
    public ServletContextBeanArchiveHandler(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public BeanArchiveBuilder handle(String path) {
        if (!path.equals(WebAppBeanArchiveScanner.WEB_INF_CLASSES)) {
            return null;
        }

        BeanArchiveBuilder builder = new BeanArchiveBuilder();
        handleResourcePath(path, path, builder);
        return builder;
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
                    // Class file
                    String className = toClassName(rootPath, subpath);
                    builder.addClass(className);
                    WeldServletLogger.LOG.tracev("Class discovered: {0}", className);
                }
            }
        }
    }

    private String toClassName(String rootPath, String resourcePath) {
        // Remove WEB-INF/classes part, suffix and replace slashes with dots
        return resourcePath.substring(rootPath.length() + 1, resourcePath.lastIndexOf(Files.CLASS_FILE_EXTENSION)).replace(SLASH, DOT);
    }

}
