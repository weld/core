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

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Allows a bean to obtain information about itself.
 *
 * @author Jozef Hartinger
 * @see CDI-92
 */
public class BeanMetadataBean extends AbstractBuiltInMetadataBean<Bean<?>> {

    public BeanMetadataBean(BeanManagerImpl beanManager) {
        this(Bean.class.getSimpleName(), beanManager);
    }

    protected BeanMetadataBean(String idSuffix, BeanManagerImpl beanManager) {
        super(idSuffix, Reflections.<Class<Bean<?>>> cast(Bean.class), beanManager);
    }

    @Override
    protected Bean<?> newInstance(InjectionPoint ip, CreationalContext<Bean<?>> creationalContext) {
        Contextual<?> contextual = getParentCreationalContext(creationalContext).getContextual();
        if (contextual instanceof Bean<?>) {
            return Reflections.cast(contextual);
        } else {
            throw new IllegalArgumentException("Unable to determine Bean metadata for " + ip);
        }
    }
}
