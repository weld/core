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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.bean.SerializableForwardingInjectionPoint;

/**
 * Bean for InjectionPoint metadata
 *
 * @author David Allen
 */
public class InjectionPointBean extends AbstractStaticallyDecorableBuiltInBean<InjectionPoint> {

    /**
     * Creates an InjectionPoint Web Bean for the injection of the containing bean owning
     * the field, constructor or method for the annotated item
     *
     * @param manager The RI manager implementation
     */
    public InjectionPointBean(BeanManagerImpl manager) {
        super(manager, InjectionPoint.class);
    }

    @Override
    protected InjectionPoint newInstance(InjectionPoint ip, CreationalContext<InjectionPoint> creationalContext) {
        if (ip instanceof SerializableForwardingInjectionPoint || ip == null) {
            return ip;
        }
        ip = new SerializableForwardingInjectionPoint(getBeanManager().getContextId(), ip);
        return ip;
    }

    @Override
    public String toString() {
        return "Implicit Bean [javax.enterprise.inject.spi.InjectionPoint] with qualifiers [@Default]";
    }

}
