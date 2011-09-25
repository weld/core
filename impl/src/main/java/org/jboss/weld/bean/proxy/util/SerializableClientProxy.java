/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.weld.bean.proxy.util;

import org.jboss.weld.Container;
import org.jboss.weld.serialization.spi.ContextualStore;

import javax.enterprise.inject.spi.Bean;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * A wrapper mostly for client proxies which provides header information useful
 * to generate the client proxy class in a VM before the proxy object is
 * deserialized. Only client proxies really need this extra step for
 * serialization and deserialization since the other proxy classes are generated
 * during bean archive deployment.
 *
 * @author David Allen
 */
public class SerializableClientProxy implements Serializable {

    private static final long serialVersionUID = -46820068707447753L;

    private final String beanId;
    private final String containerId;

    public SerializableClientProxy(final String beanId, final String containerId) {
        this.beanId = beanId;
        this.containerId = containerId;
    }

    /**
     * Always returns the original proxy object that was serialized.
     *
     * @return the proxy object
     * @throws java.io.ObjectStreamException
     */
    Object readResolve() throws ObjectStreamException {
        Bean<?> bean = Container.instance(containerId).services().get(ContextualStore.class).<Bean<Object>, Object>getContextual(beanId);
        return Container.instance().deploymentManager().getClientProxyProvider().getClientProxy(bean);
    }

}
