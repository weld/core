/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
import java.util.Set;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;

import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.context.http.HttpLiteral;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.ExpressionLanguageSupport;
import org.jboss.weld.module.WeldModule;
import org.jboss.weld.module.web.context.http.HttpRequestContextImpl;
import org.jboss.weld.module.web.context.http.HttpSessionContextImpl;
import org.jboss.weld.module.web.context.http.HttpSessionDestructionContext;
import org.jboss.weld.module.web.context.http.LazyHttpConversationContextImpl;
import org.jboss.weld.module.web.el.WeldELResolver;
import org.jboss.weld.module.web.el.WeldExpressionFactory;
import org.jboss.weld.module.web.servlet.ServletApiAbstraction;
import org.jboss.weld.module.web.servlet.ServletContextService;
import org.jboss.weld.resources.WeldClassLoaderResourceLoader;
import org.jboss.weld.serialization.BeanIdentifierIndex;
import org.jboss.weld.util.Bindings;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Weld module that provides Servlet and EL integration.
 *
 * @author Jozef Hartinger
 *
 */
public class WeldWebModule implements WeldModule {

    public static final ExpressionLanguageSupport EL_SUPPORT = new ExpressionLanguageSupport() {
        @Override
        public void cleanup() {
        }

        @Override
        public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory) {
            return new WeldExpressionFactory(expressionFactory);
        }

        @Override
        public ELResolver createElResolver(BeanManagerImpl manager) {
            return new WeldELResolver(manager);
        }
    };

    @Override
    public String getName() {
        return "weld-web";
    }

    @Override
    public void postServiceRegistration(PostServiceRegistrationContext ctx) {
        ctx.getServices().add(ExpressionLanguageSupport.class, EL_SUPPORT);
        ctx.getServices().add(ServletContextService.class, new ServletContextService());
        ctx.getServices().add(ServletApiAbstraction.class, new ServletApiAbstraction(WeldClassLoaderResourceLoader.INSTANCE));
    }

    @Override
    public void postContextRegistration(PostContextRegistrationContext ctx) {
        final BeanIdentifierIndex index = ctx.getServices().get(BeanIdentifierIndex.class);
        final String contextId = ctx.getContextId();
        if (Reflections.isClassLoadable(ServletApiAbstraction.SERVLET_CONTEXT_CLASS_NAME,
                WeldClassLoaderResourceLoader.INSTANCE)) {
            // Register the Http contexts if not in
            Set<Annotation> httpQualifiers = ImmutableSet.<Annotation> builder().addAll(Bindings.DEFAULT_QUALIFIERS)
                    .add(HttpLiteral.INSTANCE).build();
            ctx.addContext(new ContextHolder<HttpSessionContext>(new HttpSessionContextImpl(contextId, index),
                    HttpSessionContext.class, httpQualifiers));
            ctx.addContext(new ContextHolder<HttpSessionDestructionContext>(new HttpSessionDestructionContext(contextId, index),
                    HttpSessionDestructionContext.class, httpQualifiers));
            ctx.addContext(new ContextHolder<HttpConversationContext>(
                    new LazyHttpConversationContextImpl(contextId, ctx.getServices()), HttpConversationContext.class,
                    httpQualifiers));
            ctx.addContext(new ContextHolder<HttpRequestContext>(new HttpRequestContextImpl(contextId),
                    HttpRequestContext.class, httpQualifiers));
        }
    }

    @Override
    public void preBeanRegistration(PreBeanRegistrationContext ctx) {
        if (Reflections.isClassLoadable(ServletApiAbstraction.SERVLET_CONTEXT_CLASS_NAME,
                WeldClassLoaderResourceLoader.INSTANCE)) {
            ctx.registerBean(new HttpServletRequestBean(ctx.getBeanManager()));
            ctx.registerBean(new HttpSessionBean(ctx.getBeanManager()));
            ctx.registerBean(new ServletContextBean(ctx.getBeanManager()));
        }
    }
}
