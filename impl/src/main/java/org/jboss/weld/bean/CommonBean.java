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
package org.jboss.weld.bean;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.bean.ForwardingBeanAttributes;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Common superclass for beans that are identified using id.
 *
 * @author Jozef Hartinger
 * @author Pete Muir
 *
 */
public abstract class CommonBean<T> extends ForwardingBeanAttributes<T> implements Bean<T> {

    public static final String BEAN_ID_PREFIX = RIBean.class.getPackage().getName();

    public static final String BEAN_ID_SEPARATOR = "-";

    private final String id;

    private final int hashCode;

    private BeanAttributes<T> attributes;

    protected CommonBean(BeanAttributes<T> attributes, String idSuffix, BeanManagerImpl beanManager) {
        this.attributes = attributes;
        this.id = new StringBuilder().append(BEAN_ID_PREFIX).append(BEAN_ID_SEPARATOR).append(beanManager.getId()).append(BEAN_ID_SEPARATOR).append(idSuffix).toString();
        this.hashCode = this.id.hashCode();
    }

    protected Object unwrap(Object object) {
        if (object instanceof ForwardingBean<?>) {
            return Reflections.<ForwardingBean<?>> cast(object).delegate();
        }
        if (object instanceof ForwardingInterceptor<?>) {
            return Reflections.<ForwardingInterceptor<?>> cast(object).delegate();
        }
        if (object instanceof ForwardingDecorator<?>) {
            return Reflections.<ForwardingDecorator<?>> cast(object).delegate();
        }
        return object;
    }

    @Override
    public boolean equals(Object obj) {
        Object object = unwrap(obj);
        if (object instanceof CommonBean<?>) {
            CommonBean<?> that = (CommonBean<?>) object;
            return this.getId().equals(that.getId());
        } else {
            return false;
        }
    }

    protected BeanAttributes<T> attributes() {
        return attributes;
    }

    public void setAttributes(BeanAttributes<T> attributes) {
        this.attributes = attributes;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
