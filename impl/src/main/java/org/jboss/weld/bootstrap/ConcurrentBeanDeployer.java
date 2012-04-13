/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.bootstrap;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeFactory;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeImpl;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.executor.IterativeWorkerTaskFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.BeansClosure;
import org.jboss.weld.util.collections.ConcurrentHashSetSupplier;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

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
        super(manager, ejbDescriptors, services, BeanDeployerEnvironment.newConcurrentEnvironment(ejbDescriptors, manager));
        this.executor = services.get(ExecutorServices.class);
    }

    @Override
    public BeanDeployer addClasses(Iterable<String> c) {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<String>(c) {
            protected void doWork(String className) {
                addClass(className);
            }
        });
        return this;
    }

    @Override
    public void processAnnotatedTypes() {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<AnnotatedType<?>>(getEnvironment().getAnnotatedTypes()) {
            protected void doWork(AnnotatedType<?> annotatedType) {
                // fire event
                boolean synthetic = getEnvironment().getAnnotatedTypeSource(annotatedType) != null;
                ProcessAnnotatedTypeImpl<?> event;
                if (synthetic) {
                    event = ProcessAnnotatedTypeFactory.create(getManager(), annotatedType, getEnvironment().getAnnotatedTypeSource(annotatedType));
                } else {
                    event = ProcessAnnotatedTypeFactory.create(getManager(), annotatedType);
                }
                event.fire();
                // process the result
                if (event.isVeto()) {
                    getEnvironment().vetoAnnotatedType(annotatedType);
                } else {
                    boolean dirty = event.isDirty();
                    if (dirty) {
                        getEnvironment().removeAnnotatedType(annotatedType); // remove the original class
                        AnnotatedType<?> modifiedType = event.getAnnotatedType();
                        if (modifiedType instanceof SlimAnnotatedType<?>) {
                            annotatedType = modifiedType;
                        } else {
                            annotatedType = classTransformer.getAnnotatedType(modifiedType);
                        }
                    }

                    // vetoed due to @Veto or @Requires
                    boolean vetoed = Beans.isVetoed(annotatedType);

                    if (dirty && !vetoed) {
                        getEnvironment().addAnnotatedType(annotatedType); // add a replacement for the removed class
                    }
                    if (!dirty && vetoed) {
                        getEnvironment().vetoAnnotatedType(annotatedType);
                    }
                }
            }
        });
    }

    @Override
    protected void processBeanAttributes(Collection<? extends AbstractBean<?, ?>> beans) {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<AbstractBean<?, ?>>(beans) {
            protected void doWork(AbstractBean<?, ?> bean) {
                // fire ProcessBeanAttributes for class beans
                boolean vetoed = fireProcessBeanAttributes(bean);
                if (vetoed) {
                    if (bean.isSpecializing()) {
                        BeansClosure.getClosure(getManager()).removeSpecialized(bean.getSpecializedBean());
                        getQueue().add(bean.getSpecializedBean());
                    }
                    getEnvironment().vetoBean(bean);
                } else {
                    // now that we know that the bean won't be vetoed, it's the right time to register @New injection points
                    getEnvironment().addNewBeansFromInjectionPoints(bean);
                }
            }
        });
    }

    @Override
    public void createClassBeans() {
        final Multimap<Class<?>, AnnotatedType<?>> otherWeldClasses = Multimaps.newSetMultimap(new ConcurrentHashMap<Class<?>, Collection<AnnotatedType<?>>>(),
                new ConcurrentHashSetSupplier<AnnotatedType<?>>());

        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<AnnotatedType<?>>(getEnvironment().getAnnotatedTypes()) {
            protected void doWork(AnnotatedType<?> weldClass) {
                createClassBean(weldClass, otherWeldClasses);
            }
        });

        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<InternalEjbDescriptor<?>>(getEnvironment().getEjbDescriptors()) {
            protected void doWork(InternalEjbDescriptor<?> descriptor) {
                if (!getEnvironment().isVetoed(descriptor.getBeanClass())) {
                    if (descriptor.isSingleton() || descriptor.isStateful() || descriptor.isStateless()) {
                        if (otherWeldClasses.containsKey(descriptor.getBeanClass())) {
                            for (AnnotatedType<?> annotatedType : otherWeldClasses.get(descriptor.getBeanClass())) {
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
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<AbstractClassBean<?>>(getEnvironment().getClassBeanMap().values()) {
            protected void doWork(AbstractClassBean<?> bean) {
                createObserversProducersDisposers(bean);
            }
        });
    }

    @Override
    public void doAfterBeanDiscovery(List<? extends Bean<?>> beanList) {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<Bean<?>>(beanList) {
            protected void doWork(Bean<?> bean) {
                if (bean instanceof RIBean<?>) {
                    ((RIBean<?>) bean).initializeAfterBeanDiscovery();
                }
            }
        });
    }

    @Override
    public AbstractBeanDeployer<BeanDeployerEnvironment> initializeBeans() {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<RIBean<?>>(getEnvironment().getBeans()) {
            protected void doWork(RIBean<?> bean) {
                bean.initialize(getEnvironment());
            }
        });
        return this;
    }

    @Override
    public AbstractBeanDeployer<BeanDeployerEnvironment> fireBeanEvents() {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<RIBean<?>>(getEnvironment().getBeans()) {
            protected void doWork(RIBean<?> bean) {
                fireBeanEvents(bean);
            }
        });
        return this;
    }
}
