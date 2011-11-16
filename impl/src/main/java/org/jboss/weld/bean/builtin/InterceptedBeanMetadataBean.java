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
package org.jboss.weld.bean.builtin;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.literal.InterceptedLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Allows an interceptor to obtain information about the bean it intercepts.
 *
 * @author Jozef Hartinger
 * @see CDI-92
 *
 */
public class InterceptedBeanMetadataBean extends BeanMetadataBean {

    public InterceptedBeanMetadataBean(BeanManagerImpl beanManager) {
        this(Intercepted.class.getSimpleName() + RIBean.BEAN_ID_SEPARATOR + Bean.class.getSimpleName(), beanManager);
    }

    protected InterceptedBeanMetadataBean(String idSuffix, BeanManagerImpl beanManager) {
        super(idSuffix, beanManager);
    }

    @Override
    protected Bean<?> newInstance(InjectionPoint ip, CreationalContext<Bean<?>> ctx) {
        checkInjectionPoint(ip);

        WeldCreationalContext<?> interceptorContext = getParentCreationalContext(ctx);
        WeldCreationalContext<?> interceptedBeanContext = getParentCreationalContext(interceptorContext);
        Contextual<?> interceptedContextual = interceptedBeanContext.getContextual();

        if (interceptedContextual instanceof Bean<?>) {
            return Reflections.cast(interceptedContextual);
        } else {
            throw new IllegalArgumentException("Unable to determine @Intercepted Bean<?> for this interceptor.");
        }
    }

    protected void checkInjectionPoint(InjectionPoint ip) {
        if (!(ip.getBean() instanceof Interceptor<?>)) {
            throw new IllegalArgumentException("@Intercepted Bean<?> can only be injected into an interceptor.");
        }
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.<Annotation> singleton(InterceptedLiteral.INSTANCE);
    }

    @Override
    public String toString() {
        return "Implicit Bean [javax.enterprise.inject.spi.Bean] with qualifiers [@Intercepted]";
    }
}
