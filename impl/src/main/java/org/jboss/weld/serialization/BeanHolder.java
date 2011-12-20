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
package org.jboss.weld.serialization;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.Container;
import org.jboss.weld.serialization.spi.ContextualStore;

/**
 * Serializable holder that keeps reference to a bean and is capable of reloading the reference on deserialization.
 *
 * @author Jozef Hartinger
 *
 * @param <T> bean type
 */
public class BeanHolder<T> implements SerializableHolder<Bean<T>> {

    public static <T> BeanHolder<T> of(Bean<T> bean) {
        return new BeanHolder<T>(bean);
    }

    private static final long serialVersionUID = -3726981726872809169L;

    private transient Bean<T> bean;
    private final String beanId;

    public BeanHolder(Bean<T> bean) {
        this.bean = bean;
        if (bean == null) {
            beanId = null;
        } else {
            beanId = Container.instance().services().get(ContextualStore.class).putIfAbsent(bean);
        }
    }

    // Serialization
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (beanId != null) {
            bean = Container.instance().services().get(ContextualStore.class).<Bean<T>, T> getContextual(beanId);
        }
    }

    public Bean<T> get() {
        return bean;
    }
}
