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
package org.jboss.weld.serialization;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.Container;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;

/**
 * Serializable holder that keeps reference to a bean and is capable of reloading the reference on deserialization.
 *
 * @author Jozef Hartinger
 *
 * @param <T> bean type
 */
public class BeanHolder<T> extends AbstractSerializableHolder<Bean<T>> {

    private static final long serialVersionUID = 6039992808930111222L;

    public static <T> BeanHolder<T> of(String contextId, Bean<T> bean) {
        return new BeanHolder<T>(contextId, bean);
    }

    private final String contextId;
    private final BeanIdentifier beanId;

    public BeanHolder(String contextId, Bean<T> bean) {
        super(bean);
        this.contextId = contextId;
        if (bean == null) {
            beanId = null;
        } else {
            beanId = Container.instance(contextId).services().get(ContextualStore.class).putIfAbsent(bean);
        }
    }

    @Override
    protected Bean<T> initialize() {
        if (beanId == null) {
            return null;
        }
        return Container.instance(contextId).services().get(ContextualStore.class).<Bean<T>, T> getContextual(beanId);
    }
}
