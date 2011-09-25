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
package org.jboss.weld.bootstrap;

import org.jboss.weld.Container;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.event.ObserverFactory;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.DeploymentStructures;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Extension;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jboss.weld.util.reflection.Reflections.cast;

/**
 * @author pmuir
 */
public class ExtensionBeanDeployer {

    private final BeanManagerImpl beanManager;
    private final Set<Metadata<Extension>> extensions;
    private final Deployment deployment;
    private final Map<BeanDeploymentArchive, BeanDeployment> beanDeployments;
    private final Collection<ContextHolder<? extends Context>> contexts;

    public ExtensionBeanDeployer(BeanManagerImpl manager, Deployment deployment, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Collection<ContextHolder<? extends Context>> contexts) {
        this.beanManager = manager;
        this.extensions = new HashSet<Metadata<Extension>>();
        this.deployment = deployment;
        this.beanDeployments = beanDeployments;
        this.contexts = contexts;
    }

    public ExtensionBeanDeployer deployBeans() {
        ClassTransformer classTransformer = Container.instance(beanManager.getContextId()).services().get(ClassTransformer.class);
        for (Metadata<Extension> extension : extensions) {
            WeldClass<Extension> clazz = cast(classTransformer.loadClass(extension.getValue().getClass()));

            // Locate the BeanDeployment for this extension
            BeanDeployment beanDeployment = DeploymentStructures.getOrCreateBeanDeployment(deployment, beanManager, beanDeployments, contexts, clazz.getJavaClass());

            ExtensionBean bean = new ExtensionBean(beanDeployment.getBeanManager(), clazz, extension);
            Set<ObserverMethodImpl<?, ?>> observerMethods = new HashSet<ObserverMethodImpl<?, ?>>();
            createObserverMethods(bean, beanDeployment.getBeanManager(), clazz, observerMethods);
            beanDeployment.getBeanManager().addBean(bean);
            for (ObserverMethodImpl<?, ?> observerMethod : observerMethods) {
                observerMethod.initialize();
                beanDeployment.getBeanManager().addObserver(observerMethod);
            }
        }
        return this;
    }


    public void addExtensions(Iterable<Metadata<Extension>> extensions) {
        for (Metadata<Extension> extension : extensions) {
            addExtension(extension);
        }
    }

    public void addExtension(Metadata<Extension> extension) {
        this.extensions.add(extension);
    }

    protected <X> void createObserverMethods(RIBean<X> declaringBean, BeanManagerImpl beanManager, WeldClass<? super X> annotatedClass, Set<ObserverMethodImpl<?, ?>> observerMethods) {
        for (WeldMethod<?, ? super X> method : Beans.getObserverMethods(annotatedClass)) {
            createObserverMethod(declaringBean, beanManager, method, observerMethods);
        }
    }

    protected <T, X> void createObserverMethod(RIBean<X> declaringBean, BeanManagerImpl beanManager, WeldMethod<T, ? super X> method, Set<ObserverMethodImpl<?, ?>> observerMethods) {
        ObserverMethodImpl<T, X> observer = ObserverFactory.create(method, declaringBean, beanManager);
        observerMethods.add(observer);
    }

}
