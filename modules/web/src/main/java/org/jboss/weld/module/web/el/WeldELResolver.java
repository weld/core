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
package org.jboss.weld.module.web.el;

import javax.el.ELContext;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.LazyValueHolder;

/**
 * @author pmuir
 * @author Jozef Hartinger
 */
public class WeldELResolver extends AbstractWeldELResolver {

    private final BeanManagerImpl beanManager;
    private final LazyValueHolder<Namespace> rootNamespace;

    public WeldELResolver(BeanManagerImpl manager) {
        this.beanManager = manager;
        this.rootNamespace = LazyValueHolder.forSupplier(() -> new Namespace(manager.getDynamicAccessibleNamespaces()));
    }

    @Override
    protected BeanManagerImpl getManager(ELContext context) {
        return beanManager;
    }

    @Override
    protected Namespace getRootNamespace() {
        return rootNamespace.get();
    }

}
