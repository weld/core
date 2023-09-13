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

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.servlet.http.HttpServletRequest;

import org.jboss.weld.bean.builtin.AbstractStaticallyDecorableBuiltInBean;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.web.context.http.HttpRequestContextImpl;
import org.jboss.weld.module.web.logging.ServletLogger;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Built-in bean exposing {@link HttpServletRequest}.
 *
 * @author Jozef Hartinger
 *
 */
public class HttpServletRequestBean extends AbstractStaticallyDecorableBuiltInBean<HttpServletRequest> {

    public HttpServletRequestBean(BeanManagerImpl beanManager) {
        super(beanManager, HttpServletRequest.class);
    }

    @Override
    protected HttpServletRequest newInstance(InjectionPoint ip, CreationalContext<HttpServletRequest> creationalContext) {
        try {
            Context context = getBeanManager().getContext(RequestScoped.class);
            if (context instanceof HttpRequestContextImpl) {
                return Reflections.<HttpRequestContextImpl> cast(context).getHttpServletRequest();
            }
            throw ServletLogger.LOG.cannotInjectObjectOutsideOfServletRequest(HttpServletRequest.class.getSimpleName(), null);
        } catch (ContextNotActiveException e) {
            throw ServletLogger.LOG.cannotInjectObjectOutsideOfServletRequest(HttpServletRequest.class.getSimpleName(), e);
        }
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return RequestScoped.class;
    }
}
