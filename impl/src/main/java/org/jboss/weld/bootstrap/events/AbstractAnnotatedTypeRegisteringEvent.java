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
package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.DefinitionException;

import org.jboss.weld.annotated.AnnotatedTypeValidator;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.events.configurator.AnnotatedTypeConfiguratorImpl;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

public class AbstractAnnotatedTypeRegisteringEvent extends AbstractBeanDiscoveryEvent {

    protected final List<AnnotatedTypeRegistration<?>> additionalAnnotatedTypes;

    protected AbstractAnnotatedTypeRegisteringEvent(BeanManagerImpl beanManager, Type rawType,
            BeanDeploymentArchiveMapping bdaMapping, Deployment deployment,
            Collection<ContextHolder<? extends Context>> contexts) {
        super(beanManager, rawType, bdaMapping, deployment, contexts);
        this.additionalAnnotatedTypes = new LinkedList<>();
    }

    protected void addSyntheticAnnotatedType(AnnotatedType<?> type, String id) {
        AnnotatedTypeValidator.validateAnnotatedType(type);
        if (type.getJavaClass().isAnnotation() || Beans.isVetoed(type)) {
            return;
        }
        storeSyntheticAnnotatedType(getOrCreateBeanDeployment(type.getJavaClass()), type, id);
    }

    protected void storeSyntheticAnnotatedType(BeanDeployment deployment, AnnotatedType<?> type, String id) {
        deployment.getBeanDeployer().addSyntheticClass(type, getReceiver(), id);
    }

    protected void finish() {
        try {
            for (AnnotatedTypeRegistration<?> annotatedTypeRegistration : additionalAnnotatedTypes) {
                addSyntheticAnnotatedType(annotatedTypeRegistration.configurator.complete(), annotatedTypeRegistration.id);
            }
        } catch (Exception e) {
            throw new DefinitionException(e);
        }
    }

    protected static class AnnotatedTypeRegistration<T> {

        private final AnnotatedTypeConfiguratorImpl<T> configurator;

        private final String id;

        AnnotatedTypeRegistration(AnnotatedTypeConfiguratorImpl<T> configurator, String id) {
            this.configurator = configurator;
            this.id = id;
        }

    }
}
