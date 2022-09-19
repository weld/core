/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.builtin;

import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.weld.bean.BeanIdentifiers;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.Arrays2;

public class BeanManagerBean extends AbstractBuiltInBean<BeanManagerProxy> {

    private static final Set<Type> TYPES = Arrays2.<Type>asSet(Object.class, BeanContainer.class, BeanManager.class);

    public BeanManagerBean(BeanManagerImpl manager) {
        super(new StringBeanIdentifier(BeanIdentifiers.forBuiltInBean(manager, BeanManager.class, null)), manager, BeanManagerProxy.class);
    }

    public BeanManagerProxy create(CreationalContext<BeanManagerProxy> creationalContext) {
        return new BeanManagerProxy(getBeanManager());
    }

    @Override
    public Class<BeanManagerProxy> getType() {
        return BeanManagerProxy.class;
    }

    public Set<Type> getTypes() {
        return TYPES;
    }

    @Override
    public String toString() {
        return "Built-in Bean [jakarta.enterprise.inject.spi.BeanManager] with qualifiers [@Default]";
    }
}
