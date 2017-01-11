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
package org.jboss.weld.test.util.el;

import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.web.el.WeldELContextListener;
import org.jboss.weld.module.web.el.WeldExpressionFactory;

/**
 * Access to EL
 *
 * @author Gavin King
 */
public class EL {

    public static final ExpressionFactory EXPRESSION_FACTORY = new WeldExpressionFactory(ExpressionFactory.newInstance());

    public static final ELContextListener[] EL_CONTEXT_LISTENERS = { new WeldELContextListener() };

    private EL() {
    }

    public static ELContext createELContext(BeanManagerImpl beanManagerImpl) {
        StandardELContext context = new StandardELContext(EXPRESSION_FACTORY);
        context.addELResolver(beanManagerImpl.getELResolver());
        callELContextListeners(context);
        return context;
    }

    public static void callELContextListeners(ELContext context) {
        ELContextEvent event = new ELContextEvent(context);
        for (ELContextListener listener : EL_CONTEXT_LISTENERS) {
            listener.contextCreated(event);
        }
    }

}
