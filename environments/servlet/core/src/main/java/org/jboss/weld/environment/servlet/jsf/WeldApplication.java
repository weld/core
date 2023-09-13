/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.servlet.jsf;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

import org.jboss.weld.environment.servlet.WeldServletLifecycle;
import org.jboss.weld.environment.servlet.logging.WeldServletLogger;
import org.jboss.weld.environment.servlet.util.ForwardingELResolver;
import org.jboss.weld.environment.servlet.util.TransparentELResolver;
import org.jboss.weld.module.web.el.WeldELContextListener;

/**
 * @author Pete Muir
 * @author Dan Allen
 * @author Ales Justin
 */
public class WeldApplication extends ApplicationWrapper {
    /**
     * The BeanManager may not have been initialized at the time JSF is initializing. Therefore,
     * we stick in a ForwardingELResolver that delegates to the BeanManager ELResolver, which will
     * be plugged in when it's available. If the ELResolver is invoked before the BeanManager
     * is available, the resolver will perform no action (and thus produce no result).
     */
    private static class LazyBeanManagerIntegrationELResolver extends ForwardingELResolver {
        private ELResolver delegate;

        public LazyBeanManagerIntegrationELResolver() {
            delegate = new TransparentELResolver();
        }

        public void beanManagerReady(BeanManager beanManager) {
            this.delegate = beanManager.getELResolver();
        }

        @Override
        protected ELResolver delegate() {
            return delegate;
        }
    }

    private LazyBeanManagerIntegrationELResolver elResolver;
    private ExpressionFactory expressionFactory;
    private BeanManager beanManager;

    public WeldApplication(Application application) {
        super(application);
        super.addELContextListener(new WeldELContextListener());
        elResolver = new LazyBeanManagerIntegrationELResolver();
        super.addELResolver(elResolver);
    }

    private void init() {
        ExpressionFactory expressionFactory = this.expressionFactory;
        BeanManager beanManager = null;
        if (expressionFactory == null && (expressionFactory = super.getExpressionFactory()) != null
                && (beanManager = beanManager()) != null) {
            elResolver.beanManagerReady(beanManager);
            this.expressionFactory = beanManager.wrapExpressionFactory(expressionFactory);
        }
    }

    @Override
    public ExpressionFactory getExpressionFactory() {
        init();
        if (expressionFactory == null) {
            return super.getExpressionFactory();
        } else {
            return expressionFactory;
        }
    }

    private BeanManager beanManager() {
        FacesContext facesContext;
        if (beanManager == null && (facesContext = FacesContext.getCurrentInstance()) != null) {
            Object obj = facesContext.getExternalContext().getContext();
            boolean notFound = false;
            try {
                if (obj instanceof ServletContext) {
                    final ServletContext ctx = (ServletContext) obj;
                    final BeanManager tmp = (BeanManager) ctx.getAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME);
                    if (tmp == null) {
                        return null;
                    }
                    this.beanManager = tmp;
                } else {
                    notFound = true;
                }
            } catch (Throwable t) {
                throw WeldServletLogger.LOG.exceptionFetchingBeanManager(t);
            }
            if (notFound) {
                throw WeldServletLogger.LOG.notInAServletOrPortlet();
            }
        }
        return beanManager;
    }

}
