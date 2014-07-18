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

import static org.jboss.weld.logging.Category.CONTEXT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;

import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.beanstore.http.RequestBeanStore;
import org.jboss.weld.context.cache.RequestScopedBeanCache;
import org.jboss.weld.logging.messages.ContextMessage;
import org.slf4j.cal10n.LocLogger;

import javax.enterprise.context.RequestScoped;
import javax.servlet.ServletRequest;

import java.lang.annotation.Annotation;

public class HttpRequestContextImpl extends AbstractBoundContext<ServletRequest> implements HttpRequestContext {

    private static final LocLogger log = loggerFactory().getLogger(CONTEXT);

    private final NamingScheme namingScheme;

    /**
     * Constructor
     */
    public HttpRequestContextImpl() {
        super(false);
        this.namingScheme = new SimpleNamingScheme(HttpRequestContext.class.getName());
    }

    public boolean associate(ServletRequest request) {
        // At this point the bean store should never be set - see also WeldListener#nestedInvocationGuard
       if (getBeanStore() != null) {
           log.warn(ContextMessage.BEAN_STORE_LEAK_DURING_ASSOCIATION, this.getClass().getName(), request);
       }
       // We always associate a new bean store to avoid possible leaks (security threats)
       setBeanStore(new RequestBeanStore(request, namingScheme));
       getBeanStore().attach();
       return true;
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

}
