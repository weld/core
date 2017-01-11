/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionListener;

/**
 * Holds the session associated with the current request.
 *
 * This utility class was added to work around an incompatibility problem with some Servlet containers (JBoss Web, Tomcat). In
 * these containers, {@link HttpServletRequest#getSession(boolean)} cannot be used within
 * {@link HttpSessionListener#sessionCreated(HttpSession)} method invocation is the created session is not made available.
 * As a result either null is returned or a new session is created (possibly causing an endless loop).
 *
 * This utility class receives an {@link HttpSession} once it is created and holds it until the request is destroyed / session
 * is invalidated.
 *
 * @see https://issues.jboss.org/browse/AS7-6428
 *
 * @author Jozef Hartinger
 *
 */
public class SessionHolder {

    private static final ThreadLocal<HttpSession> CURRENT_SESSION = new ThreadLocal<HttpSession>();

    private SessionHolder() {
    }

    public static void requestInitialized(HttpServletRequest request) {
        CURRENT_SESSION.set(request.getSession(false));
    }

    public static void sessionCreated(HttpSession session) {
        CURRENT_SESSION.set(session);
    }

    public static HttpSession getSessionIfExists() {
        return CURRENT_SESSION.get();
    }

    public static HttpSession getSession(HttpServletRequest request, boolean create) {
        HttpSession session = CURRENT_SESSION.get();
        if (create && session == null) {
            request.getSession(true);
            session = CURRENT_SESSION.get();
        }
        return session;
    }

    public static void clear() {
        CURRENT_SESSION.remove();
    }
}
