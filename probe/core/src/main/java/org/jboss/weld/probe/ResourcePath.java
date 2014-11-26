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

import static org.jboss.weld.probe.Strings.IMG_PNG;
import static org.jboss.weld.probe.Strings.SUFFIX_CSS;
import static org.jboss.weld.probe.Strings.SUFFIX_HTML;
import static org.jboss.weld.probe.Strings.SUFFIX_JS;
import static org.jboss.weld.probe.Strings.SUFFIX_PNG;
import static org.jboss.weld.probe.Strings.TEXT_CSS;
import static org.jboss.weld.probe.Strings.TEXT_HTML;
import static org.jboss.weld.probe.Strings.TEXT_JAVASCRIPT;
import static org.jboss.weld.probe.Strings.TEXT_PLAIN;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Martin Kouba
 */
enum ResourcePath {

    /**
     * The root resource.
     */
    ROOT("/{\\w+\\.\\w+}"),
    /**
     * A deployment info.
     */
    DEPLOYMENT("/deployment"),
    /**
     * A collection of beans.
     */
    BEANS("/beans"),
    /**
     * A single bean.
     */
    BEAN("/beans/{.+}"),
    /**
     * A resource representing a contextual instance of a bean. This is only supported for a limited set of scopes.
     */
    BEAN_INSTANCE("/beans/{.+}/instance"),
    /**
     * A collection of observers methods.
     */
    OBSERVERS("/observers"),
    /**
     * A single observer.
     */
    OBSERVER("/observers/{.+}"),
    /**
     * A collection of inspectable contexts.
     */
    CONTEXTS("/contexts"),
    /**
     * A collection of invocation trees.
     */
    INVOCATIONS("/invocations"),
    /**
     * A single invocation tree.
     */
    INVOCATION("/invocations/{.+}"),
    /**
     * A default HTML client resource.
     */
    CLIENT_RESOURCE("/client/{\\w+\\.\\w+}"), ;

    static final String PARAM_START = "{";

    static final String PARAM_END = "}";

    private final String[] parts;

    private final String defaultContentType;

    ResourcePath(String path) {
        this(path, Strings.APPLICATION_JSON);
    }

    ResourcePath(String path, String contentType) {
        this.parts = splitPath(path);
        this.defaultContentType = contentType;
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
        return part.startsWith(PARAM_START) && part.endsWith(PARAM_END);
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

}