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

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Container lifecycle event that allows bean metadata ({@link BeanAttributes}) to be changed before the bean is registered.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the type of bean
 */
public class ProcessBeanAttributesImpl<T> extends AbstractDefinitionContainerEvent implements ProcessBeanAttributes<T> {

    protected static <T> ProcessBeanAttributesImpl<T> fire(BeanManagerImpl beanManager, BeanAttributes<T> attributes, Annotated annotated, Type type) {
        ProcessBeanAttributesImpl<T> event = new ProcessBeanAttributesImpl<T>(beanManager, attributes, annotated, type) {
        };
        event.fire();
        return event;
    }

    private ProcessBeanAttributesImpl(BeanManagerImpl beanManager, BeanAttributes<T> attributes, Annotated annotated, Type type) {
        super(beanManager, ProcessBeanAttributes.class, new Type[] { type });
        this.attributes = attributes;
        this.annotated = annotated;
    }

    private final Annotated annotated;
    private BeanAttributes<T> attributes;
    private boolean veto;
    private boolean dirty;

    @Override
    public Annotated getAnnotated() {
        return annotated;
    }

    @Override
    public BeanAttributes<T> getBeanAttributes() {
        return attributes;
    }

    @Override
    public void setBeanAttributes(BeanAttributes<T> beanAttributes) {
        attributes = beanAttributes;
        dirty = true;
    }

    @Override
    public void addDefinitionError(Throwable t) {
        getErrors().add(t);
    }

    @Override
    public void veto() {
        veto = true;
    }

    public boolean isVeto() {
        return veto;
    }

    public boolean isDirty() {
        return dirty;
    }
}
