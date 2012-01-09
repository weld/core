/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.bean.builtin.ee;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.servlet.ServletContext;

import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.messages.ServletMessage;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Built-in bean exposing {@link ServletContext}.
 *
 * @author Jozef Hartinger
 *
 */
public class ServletContextBean extends AbstractBuiltInBean<ServletContext> {

    private static ThreadLocal<ServletContext> servletContext = new ThreadLocal<ServletContext>();

    public ServletContextBean(BeanManagerImpl beanManager) {
        super(ServletContextBean.class.getName(), beanManager);
    }

    @Override
    public ServletContext create(CreationalContext<ServletContext> creationalContext) {
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
    public Set<Type> getTypes() {
        return Collections.<Type> singleton(ServletContext.class);
    }

    @Override
    public Class<ServletContext> getType() {
        return ServletContext.class;
    }

    public static void setServletContext(ServletContext ctx) {
        servletContext.set(ctx);
    }

    public static void cleanup() {
        servletContext.remove();
    }
}
