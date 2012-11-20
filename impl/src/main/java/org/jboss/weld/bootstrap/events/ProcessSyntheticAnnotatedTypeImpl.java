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
package org.jboss.weld.bootstrap.events;

import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.Resolvable;

/**
 *
 * @author Jozef Hartinger
 *
 */
public class ProcessSyntheticAnnotatedTypeImpl<T> extends ProcessAnnotatedTypeImpl<T> implements ProcessSyntheticAnnotatedType<T> {

    private Extension source;

    public ProcessSyntheticAnnotatedTypeImpl(BeanManagerImpl beanManager, SlimAnnotatedType<T> annotatedType, AnnotationDiscovery discovery, Extension source) {
        super(beanManager, annotatedType, ProcessSyntheticAnnotatedType.class, annotatedType.getJavaClass(), discovery);
        this.source = source;
    }

    @Override
    protected Resolvable createResolvable(Class<?> typeArgument, AnnotationDiscovery discovery) {
        return ProcessAnnotatedTypeEventResolvable.forProcessSyntheticAnnotatedType(typeArgument, discovery);
    }

    @Override
    public Extension getSource() {
        return source;
    }
}
