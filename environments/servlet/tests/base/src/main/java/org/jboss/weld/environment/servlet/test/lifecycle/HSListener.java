/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.servlet.test.lifecycle;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class HSListener implements HttpSessionListener {

    private BeanManager getBeanManager() {
        Context context = null;
        try {
            context = new InitialContext();
            return (BeanManager) context.lookup("java:comp/env/BeanManager");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException ignored) {
                }
            }
        }
    }

    @SuppressWarnings("serial")
    public void sessionCreated(HttpSessionEvent se) {
        // Use a special qualifier to distinguish @Initialized context events
        getBeanManager().getEvent().select(HttpSession.class, new AnnotationLiteral<Lifecycle>() {
        }).fire(se.getSession());
    }

    public void sessionDestroyed(HttpSessionEvent se) {
    }
}
