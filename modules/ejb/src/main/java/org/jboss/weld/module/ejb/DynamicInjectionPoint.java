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
package org.jboss.weld.module.ejb;

import java.io.ObjectStreamException;
import java.io.Serializable;

import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.Container;
import org.jboss.weld.injection.ForwardingInjectionPoint;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * A proxy that forwards call to the current {@link CurrentInvocationInjectionPoint}.
 *
 * @author Marko Luksa
 *
 */
class DynamicInjectionPoint extends ForwardingInjectionPoint implements Serializable {

    private static final long serialVersionUID = 0L;

    private final transient CurrentInvocationInjectionPoint invocationInjectionPoint;
    private final String contextId;

    DynamicInjectionPoint(BeanManagerImpl manager) {
        this.contextId = manager.getContextId();
        this.invocationInjectionPoint = manager.getServices().get(CurrentInvocationInjectionPoint.class);
    }

    private DynamicInjectionPoint(CurrentInvocationInjectionPoint invocationInjectionPoint, String contextId) {
        this.invocationInjectionPoint = invocationInjectionPoint;
        this.contextId = contextId;
    }

    protected InjectionPoint delegate() {
        InjectionPoint injectionPoint = invocationInjectionPoint.peek();
        if (injectionPoint == null) {
            throw BeanLogger.LOG.statelessSessionBeanInjectionPointMetadataNotAvailable();
        }
        return injectionPoint;
    }

    private Object readResolve() throws ObjectStreamException {
        return new DynamicInjectionPoint(Container.instance(contextId).services().get(CurrentInvocationInjectionPoint.class),
                contextId);
    }
}
