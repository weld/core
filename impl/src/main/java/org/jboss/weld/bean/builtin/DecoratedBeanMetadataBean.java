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

import javax.enterprise.inject.Decorated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.literal.DecoratedLiteral;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Allows a decorator to obtain information about the bean it decorates.
 *
 * @author Jozef Hartinger
 * @see CDI-92
 *
 */
public class DecoratedBeanMetadataBean extends InterceptedBeanMetadataBean {

    public DecoratedBeanMetadataBean(BeanManagerImpl beanManager) {
        super(Decorated.class.getSimpleName() + RIBean.BEAN_ID_SEPARATOR + Bean.class.getSimpleName(), beanManager);
    }

    @Override
    protected void checkInjectionPoint(InjectionPoint ip) {
        if (!(ip.getBean() instanceof Decorator<?>)) {
            throw new IllegalArgumentException("@Decorated Bean<?> can only be injected into a decorator.");
        }
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.<Annotation> singleton(DecoratedLiteral.INSTANCE);
    }

    @Override
    public String toString() {
        return "Implicit Bean [javax.enterprise.inject.spi.Bean] with qualifiers [@Decorated]";
    }
}
