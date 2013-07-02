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
package org.jboss.weld.bean.builtin.ee;

import java.lang.annotation.Annotation;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.servlet.http.HttpSession;

import org.jboss.weld.bean.builtin.AbstractStaticallyDecorableBuiltInBean;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.messages.ServletMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.servlet.SessionHolder;

/**
 * Built-in bean exposing {@link HttpSession}.
 *
 * @author Jozef Hartinger
 *
 */
public class HttpSessionBean extends AbstractStaticallyDecorableBuiltInBean<HttpSession> {

    public HttpSessionBean(BeanManagerImpl manager) {
        super(manager, HttpSession.class);
    }

    @Override
    protected HttpSession newInstance(InjectionPoint ip, CreationalContext<HttpSession> creationalContext) {
        try {
            return SessionHolder.getSessionIfExists();
        } catch (IllegalStateException e) {
            throw new IllegalStateException(ServletMessage.CANNOT_INJECT_OBJECT_OUTSIDE_OF_SERVLET_REQUEST, e, HttpSession.class.getSimpleName());
        }
    }

    @Override
    public void destroy(HttpSession instance, CreationalContext<HttpSession> creationalContext) {
        // noop
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return SessionScoped.class;
    }
}
