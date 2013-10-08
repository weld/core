/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCacheValue;

import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeContext;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.executor.IterativeWorkerTaskFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.Multimaps;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.cache.LoadingCache;

/**
 * BeanDeployer that processes some of the deployment tasks in parallel. A threadsafe instance of
 * {@link BeanDeployerEnvironment} is used.
 *
 * @author Jozef Hartinger
 *
 */
public class ConcurrentBeanDeployer extends BeanDeployer {

    private final ExecutorServices executor;

    public ConcurrentBeanDeployer(BeanManagerImpl manager, EjbDescriptors ejbDescriptors, ServiceRegistry services) {
        super(manager, ejbDescriptors, services, BeanDeployerEnvironmentFactory.newConcurrentEnvironment(ejbDescriptors, manager));
        this.executor = services.get(ExecutorServices.class);
    }

    @Override
    public BeanDeployer addClasses(Iterable<String> c) {
        final AnnotatedTypeLoader loader = createAnnotatedTypeLoader();
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<String>(c) {
            @Override
            protected void doWork(String className) {
                addClass(className, loader);
            }
        });
        return this;
    }

    @Override
    public void createClassBeans() {
        final LoadingCache<Class<?>, Set<SlimAnnotatedType<?>>> otherWeldClasses = Multimaps.newConcurrentSetMultimap();

        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<SlimAnnotatedTypeContext<?>>(getEnvironment().getAnnotatedTypes()) {
            @Override
            protected void doWork(SlimAnnotatedTypeContext<?> ctx) {
                createClassBean(ctx.getAnnotatedType(), otherWeldClasses);
            }
        });

        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<InternalEjbDescriptor<?>>(getEnvironment().getEjbDescriptors()) {
            @Override
            protected void doWork(InternalEjbDescriptor<?> descriptor) {
                if (!getEnvironment().isVetoed(descriptor.getBeanClass()) && !Beans.isVetoed(descriptor.getBeanClass())) {
                    if (descriptor.isSingleton() || descriptor.isStateful() || descriptor.isStateless()) {
                        if (otherWeldClasses.getIfPresent(descriptor.getBeanClass()) != null) {
                            for (SlimAnnotatedType<?> annotatedType : getCacheValue(otherWeldClasses, descriptor.getBeanClass())) {
                                EnhancedAnnotatedType<?> weldClass = classTransformer.getEnhancedAnnotatedType(annotatedType);
                                createSessionBean(descriptor, Reflections.<EnhancedAnnotatedType> cast(weldClass));
                            }
                        } else {
                            createSessionBean(descriptor);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void createProducersAndObservers() {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<AbstractClassBean<?>>(getEnvironment().getClassBeans()) {
            @Override
            protected void doWork(AbstractClassBean<?> bean) {
                createObserversProducersDisposers(bean);
            }
        });
    }

    @Override
    public void doAfterBeanDiscovery(List<? extends Bean<?>> beanList) {
        executor.invokeAllAndCheckForExceptions(new AfterBeanDiscoveryInitializerFactory(beanList));
    }

    @Override
    public AbstractBeanDeployer<BeanDeployerEnvironment> initializeBeans() {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<RIBean<?>>(getEnvironment().getBeans()) {
            @Override
            protected void doWork(RIBean<?> bean) {
                bean.initialize(getEnvironment());
            }
        });
        return this;
    }

    private static class AfterBeanDiscoveryInitializerFactory extends IterativeWorkerTaskFactory<Bean<?>> {

        public AfterBeanDiscoveryInitializerFactory(Iterable<? extends Bean<?>> iterable) {
            super(iterable);
        }

        @Override
        protected void doWork(Bean<?> bean) {
            if (bean instanceof RIBean<?>) {
                ((RIBean<?>) bean).initializeAfterBeanDiscovery();
            }
        }
    }
}
