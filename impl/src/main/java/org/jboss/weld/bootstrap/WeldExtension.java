/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.bootstrap;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.contexts.activator.ActivateRequestContextInterceptor;
import org.jboss.weld.contexts.activator.CdiRequestContextActivatorInterceptor;
import org.jboss.weld.util.annotated.VetoedSuppressedAnnotatedType;

/**
 * This extension is used to register built-in components which should be processed as regular CDI components.
 *
 * @author Martin Kouba
 */
class WeldExtension implements Extension {

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(ActivateRequestContextInterceptor.class, beanManager),
                ActivateRequestContextInterceptor.class.getName());
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(CdiRequestContextActivatorInterceptor.class, beanManager),
                CdiRequestContextActivatorInterceptor.class.getName());
    }

}
