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
package org.jboss.weld.module.web.context.http;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.SessionScoped;
import jakarta.servlet.http.HttpSession;

import org.jboss.weld.contexts.AbstractBoundContext;
import org.jboss.weld.contexts.beanstore.NamingScheme;
import org.jboss.weld.contexts.beanstore.SimpleBeanIdentifierIndexNamingScheme;
import org.jboss.weld.module.web.context.beanstore.http.EagerSessionBeanStore;
import org.jboss.weld.serialization.BeanIdentifierIndex;

/**
 * This special http session context is necessary because HttpSessionListeners that are called when a session
 * is being destroyed outside the scope of a HTTP request, need to be able to access the session context.
 * We can't simply activate the regular HttpSessionContext, since we would need an HttpServletRequest to associate
 * and activate the context.
 *
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class HttpSessionDestructionContext extends AbstractBoundContext<HttpSession> {

    private final NamingScheme namingScheme;

    public HttpSessionDestructionContext(String contextId, BeanIdentifierIndex index) {
        super(contextId, true);
        this.namingScheme = new SimpleBeanIdentifierIndexNamingScheme(HttpSessionContextImpl.NAMING_SCHEME_PREFIX, index);
    }

    @Override
    public boolean associate(HttpSession session) {
        if (getBeanStore() == null) {
            // Don't reassociate
            setBeanStore(new EagerSessionBeanStore(namingScheme, session, getServiceRegistry()));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return SessionScoped.class;
    }
}
