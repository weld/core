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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.enablement.GlobalEnablementBuilder;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.manager.BeanManagerImpl;

public class AfterTypeDiscoveryImpl extends AbstractBeanDiscoveryEvent implements AfterTypeDiscovery {

    public static void fire(BeanManagerImpl beanManager, Deployment deployment, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Collection<ContextHolder<? extends Context>> contexts) {
        new AfterTypeDiscoveryImpl(beanManager, beanDeployments, deployment, contexts).fire();
    }

    private final GlobalEnablementBuilder builder;

    protected AfterTypeDiscoveryImpl(BeanManagerImpl beanManager, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Deployment deployment, Collection<ContextHolder<? extends Context>> contexts) {
        super(beanManager, AfterTypeDiscovery.class, beanDeployments, deployment, contexts);
        this.builder = beanManager.getServices().get(GlobalEnablementBuilder.class);
    }

    @Override
    public List<Class<?>> getAlternatives() {
        return builder.getAlternativeList();
    }

    @Override
    public List<Class<?>> getInterceptors() {
        return builder.getInterceptorList();
    }

    @Override
    public List<Class<?>> getDecorators() {
        return builder.getDecoratorList();
    }

    @Override
    public void addAnnotatedType(AnnotatedType<?> type) {
        throw new UnsupportedOperationException();
    }
}
