/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.web;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Enumeration;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.jboss.weld.bean.builtin.AbstractStaticallyDecorableBuiltInBean;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.web.logging.ServletLogger;
import org.jboss.weld.module.web.servlet.SessionHolder;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Built-in bean exposing {@link HttpSession}.
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 */
@SuppressWarnings("deprecation")
public class HttpSessionBean extends AbstractStaticallyDecorableBuiltInBean<HttpSession> {

    public HttpSessionBean(BeanManagerImpl manager) {
        super(manager, HttpSession.class);
    }

    @Override
    protected HttpSession newInstance(InjectionPoint ip, CreationalContext<HttpSession> creationalContext) {
        return new SerializableProxy();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return SessionScoped.class;
    }

    private static class SerializableProxy implements HttpSession, Serializable {

        private static final long serialVersionUID = -617233973786462227L;

        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "False positive from FindBugs - field is set lazily.")
        private transient volatile HttpSession session;

        private SerializableProxy() {
            this.session = obtainHttpSession();
        }

        @Override
        public long getCreationTime() {
            return session().getCreationTime();
        }

        @Override
        public String getId() {
            return session().getId();
        }

        @Override
        public long getLastAccessedTime() {
            return session().getLastAccessedTime();
        }

        @Override
        public ServletContext getServletContext() {
            return session().getServletContext();
        }

        @Override
        public void setMaxInactiveInterval(int interval) {
            session().setMaxInactiveInterval(interval);
        }

        @Override
        public int getMaxInactiveInterval() {
            return session().getMaxInactiveInterval();
        }

        @Override
        public HttpSessionContext getSessionContext() {
            return session().getSessionContext();
        }

        @Override
        public Object getAttribute(String name) {
            return session().getAttribute(name);
        }

        @Override
        public Object getValue(String name) {
            return session().getValue(name);
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return session().getAttributeNames();
        }

        @Override
        public String[] getValueNames() {
            return session().getValueNames();
        }

        @Override
        public void setAttribute(String name, Object value) {
            session().setAttribute(name, value);
        }

        @Override
        public void putValue(String name, Object value) {
            session().putValue(name, value);
        }

        @Override
        public void removeAttribute(String name) {
            session().removeAttribute(name);
        }

        @Override
        public void removeValue(String name) {
            session().removeValue(name);
        }

        @Override
        public void invalidate() {
            session().invalidate();
        }

        @Override
        public boolean isNew() {
            return session().isNew();
        }

        private HttpSession session() {
            if (session == null) {
                synchronized (this) {
                    if (session == null) {
                        session = obtainHttpSession();
                    }
                }
            }
            return session;
        }

        private HttpSession obtainHttpSession() {
            HttpSession session = SessionHolder.getSessionIfExists();
            if (session == null) {
                throw ServletLogger.LOG.cannotInjectObjectOutsideOfServletRequest(HttpSession.class.getSimpleName(), null);
            }
            return session;
        }

    }
}
