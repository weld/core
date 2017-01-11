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
package org.jboss.weld.tck;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.VariableMapper;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.cdi.tck.spi.EL;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.web.el.WeldELContextListener;
import org.jboss.weld.module.web.el.WeldExpressionFactory;

/**
 *
 *
 */
public class ELImpl implements EL {

    public static final ExpressionFactory EXPRESSION_FACTORY = new WeldExpressionFactory(ExpressionFactory.newInstance());

    public static final ELContextListener[] EL_CONTEXT_LISTENERS = { new WeldELContextListener() };

    @SuppressWarnings("unchecked")
    public <T> T evaluateValueExpression(BeanManager beanManager, String expression, Class<T> expectedType) {
        ELContext elContext = createELContext(beanManager);
        return (T) EXPRESSION_FACTORY.createValueExpression(elContext, expression, expectedType).getValue(elContext);
    }

    @SuppressWarnings("unchecked")
    public <T> T evaluateMethodExpression(BeanManager beanManager, String expression, Class<T> expectedType,
            Class<?>[] expectedParamTypes, Object[] expectedParams) {
        ELContext elContext = createELContext(beanManager);
        return (T) EXPRESSION_FACTORY.createMethodExpression(elContext, expression, expectedType, expectedParamTypes).invoke(
                elContext, expectedParams);
    }

    public ELContext createELContext(BeanManager beanManager) {
        if (beanManager instanceof BeanManagerProxy) {
            BeanManagerProxy proxy = (BeanManagerProxy) beanManager;
            beanManager = proxy.delegate();
        }
        if (beanManager instanceof BeanManagerImpl) {
            return createELContext((BeanManagerImpl) beanManager);
        } else {
            throw new IllegalStateException("Wrong manager");
        }
    }

    private ELContext createELContext(BeanManagerImpl beanManagerImpl) {

        final ELResolver resolver = createELResolver(beanManagerImpl);

        ELContext context = new ELContext() {

            @Override
            public ELResolver getELResolver() {
                return resolver;
            }

            @Override
            public FunctionMapper getFunctionMapper() {
                return null;
            }

            @Override
            public VariableMapper getVariableMapper() {
                return null;
            }

        };
        callELContextListeners(context);
        return context;
    }

    private ELResolver createELResolver(BeanManagerImpl beanManagerImpl) {
        CompositeELResolver resolver = new CompositeELResolver();
        resolver.add(beanManagerImpl.getELResolver());
        resolver.add(new MapELResolver());
        resolver.add(new ListELResolver());
        resolver.add(new ArrayELResolver());
        resolver.add(new ResourceBundleELResolver());
        resolver.add(new BeanELResolver());
        return resolver;
    }

    private void callELContextListeners(ELContext context) {
        ELContextEvent event = new ELContextEvent(context);
        for (ELContextListener listener : EL_CONTEXT_LISTENERS) {
            listener.contextCreated(event);
        }
    }

}
