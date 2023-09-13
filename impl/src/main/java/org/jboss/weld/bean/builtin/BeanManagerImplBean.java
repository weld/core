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

import org.jboss.weld.bean.BeanIdentifiers;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.util.collections.Arrays2;

public class BeanManagerImplBean extends AbstractBuiltInBean<BeanManagerImpl> {

    private static final Set<Type> TYPES = Arrays2.<Type> asSet(Object.class, BeanManagerImpl.class, WeldManager.class);

    public BeanManagerImplBean(BeanManagerImpl manager) {
        super(new StringBeanIdentifier(BeanIdentifiers.forBuiltInBean(manager, BeanManagerImpl.class, null)), manager,
                BeanManagerImpl.class);
    }

    public BeanManagerImpl create(CreationalContext<BeanManagerImpl> creationalContext) {
        return getBeanManager();
    }

    @Override
    public Class<BeanManagerImpl> getType() {
        return BeanManagerImpl.class;
    }

    public Set<Type> getTypes() {
        return TYPES;
    }

    @Override
    public String toString() {
        return "Built-in Bean [org.jboss.weld.manager.BeanManagerImpl] with qualifiers [@Default]";
    }
}
