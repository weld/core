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

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.Arrays2;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.reflect.Type;
import java.util.Set;

public class BeanManagerBean extends AbstractBuiltInBean<BeanManagerImpl> {

    private static final Set<Type> TYPES = Arrays2.<Type>asSet(Object.class, BeanManagerImpl.class, BeanManager.class);

    public BeanManagerBean(BeanManagerImpl manager) {
        super(BeanManager.class.getSimpleName(), manager);
    }

    public BeanManagerImpl create(CreationalContext<BeanManagerImpl> creationalContext) {
        return getBeanManager().getCurrent();
    }

    @Override
    public Class<BeanManagerImpl> getType() {
        return BeanManagerImpl.class;
    }

    public Set<Type> getTypes() {
        return TYPES;
    }

    public void destroy(BeanManagerImpl instance, CreationalContext<BeanManagerImpl> creationalContext) {
        // No-op
    }

    @Override
    public String toString() {
        return "Built-in Bean [javax.enterprise.inject.spi.BeanManager] with qualifiers [@Default]";
    }


}