/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.ejb;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.Container;
import org.jboss.weld.injection.ForwardingInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * A proxy that forwards call to the current {@link SLSBInvocationInjectionPoint}.
 *
 * @author Marko Luksa
 *
 */
class DynamicInjectionPoint extends ForwardingInjectionPoint implements Serializable {

    private static final long serialVersionUID = 0L;

    private final transient SLSBInvocationInjectionPoint invocationInjectionPoint;
    private final String contextId;

    DynamicInjectionPoint(BeanManagerImpl manager) {
        this.contextId = manager.getContextId();
        this.invocationInjectionPoint = manager.getServices().get(SLSBInvocationInjectionPoint.class);
    }

    private DynamicInjectionPoint(SLSBInvocationInjectionPoint invocationInjectionPoint, String contextId) {
        this.invocationInjectionPoint = invocationInjectionPoint;
        this.contextId = contextId;
    }

    protected InjectionPoint delegate() {
        return invocationInjectionPoint.peek();
    }

    private Object readResolve() throws ObjectStreamException {
        return new DynamicInjectionPoint(Container.instance(contextId).services().get(SLSBInvocationInjectionPoint.class) , contextId);
    }
}
