/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.context.http;

import java.lang.annotation.Annotation;

import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;

import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.beanstore.http.RequestBeanStore;
import org.jboss.weld.context.cache.RequestScopedBeanCache;
import org.jboss.weld.util.reflection.Reflections;

public class HttpRequestContextImpl extends AbstractBoundContext<HttpServletRequest> implements HttpRequestContext {

    private static final String IDENTIFIER = HttpRequestContextImpl.class.getName();

    private final NamingScheme namingScheme;

    /**
     * Constructor
     */
    public HttpRequestContextImpl() {
        super(false);
        this.namingScheme = new SimpleNamingScheme(HttpRequestContext.class.getName());
    }

    public boolean associate(HttpServletRequest request) {
        if (request.getAttribute(IDENTIFIER) == null) {
            request.setAttribute(IDENTIFIER, IDENTIFIER);
            setBeanStore(new RequestBeanStore(request, namingScheme));
            getBeanStore().attach();
            return true;
        } else {
            return false;
        }
    }

    public boolean dissociate(HttpServletRequest request) {
        if (request.getAttribute(IDENTIFIER) != null) {
            try {
                setBeanStore(null);
                request.removeAttribute(IDENTIFIER);
                return true;
            } finally {
                cleanup();
            }
        } else {
            return false;
        }

    }

    @Override
    public void activate() {
        super.activate();
        RequestScopedBeanCache.beginRequest();
    }

    @Override
    public void deactivate() {
        try {
            RequestScopedBeanCache.endRequest();
        } finally {
            super.deactivate();
        }
    }

    public Class<? extends Annotation> getScope() {
        return RequestScoped.class;
    }

    public HttpServletRequest getHttpServletRequest() {
        if (getBeanStore() instanceof RequestBeanStore) {
            return Reflections.<RequestBeanStore>cast(getBeanStore()).getRequest();
        }
        return null;
    }
}
