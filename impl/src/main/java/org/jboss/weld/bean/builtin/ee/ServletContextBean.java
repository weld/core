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

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.servlet.ServletContext;

import org.jboss.weld.bean.builtin.AbstractStaticallyDecorableBuiltInBean;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.messages.ServletMessage;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Built-in bean exposing {@link ServletContext}.
 *
 * @author Jozef Hartinger
 *
 */
public class ServletContextBean extends AbstractStaticallyDecorableBuiltInBean<ServletContext> {

    private static ThreadLocal<ServletContext> servletContext = new ThreadLocal<ServletContext>();

    public ServletContextBean(BeanManagerImpl beanManager) {
        super(beanManager, ServletContext.class);
    }

    @Override
    protected ServletContext newInstance(InjectionPoint ip, CreationalContext<ServletContext> creationalContext) {
        if (servletContext.get() == null) {
            throw new IllegalStateException(ServletMessage.CANNOT_INJECT_OBJECT_OUTSIDE_OF_SERVLET_REQUEST, ServletContext.class.getSimpleName());
        }
        return servletContext.get();
    }

    @Override
    public void destroy(ServletContext instance, CreationalContext<ServletContext> creationalContext) {
        // noop
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return RequestScoped.class;
    }

    public static void setServletContext(ServletContext ctx) {
        servletContext.set(ctx);
    }

    public static void cleanup() {
        servletContext.remove();
    }
}
