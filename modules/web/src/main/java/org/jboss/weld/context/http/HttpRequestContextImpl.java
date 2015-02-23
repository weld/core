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
import org.jboss.weld.context.cache.RequestScopedCache;
import org.jboss.weld.logging.ContextLogger;
import org.jboss.weld.util.reflection.Reflections;

public class HttpRequestContextImpl extends AbstractBoundContext<HttpServletRequest> implements HttpRequestContext {

    private final NamingScheme namingScheme;

    /**
     * Constructor
     */
    public HttpRequestContextImpl(String contextId) {
        super(contextId, false);
        this.namingScheme = new SimpleNamingScheme(HttpRequestContext.class.getName());
    }

    public boolean associate(HttpServletRequest request) {
        // At this point the bean store should never be set - see also HttpContextLifecycle#nestedInvocationGuard
        if (getBeanStore() != null) {
            ContextLogger.LOG.beanStoreLeakDuringAssociation(this.getClass().getName(), request);
        }
        // We always associate a new bean store to avoid possible leaks (security threats)
        final RequestBeanStore beanStore = new RequestBeanStore(request, namingScheme);
        setBeanStore(beanStore);
        beanStore.attach();
        return true;
    }

    @Override
    public void activate() {
        super.activate();
        RequestScopedCache.beginRequest();
    }

    @Override
    public void deactivate() {
        try {
            RequestScopedCache.endRequest();
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
