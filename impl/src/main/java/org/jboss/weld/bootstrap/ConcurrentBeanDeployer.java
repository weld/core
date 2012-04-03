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

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.executor.IterativeWorkerTaskFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.ExecutorServices;

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

//    @Override
//    public void processAnnotatedTypes() {
//        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<WeldClass<?>>(getEnvironment().getClasses()) {
//            protected void doWork(WeldClass<?> clazz) {
//                WeldClass<?> weldClass = clazz;
//                // fire event
//                boolean synthetic = getEnvironment().getSource(weldClass) != null;
//                ProcessAnnotatedTypeImpl<?> event;
//                if (synthetic) {
//                    event = ProcessAnnotatedTypeFactory.create(getManager(), weldClass, getEnvironment().getSource(weldClass));
//                } else {
//                    event = ProcessAnnotatedTypeFactory.create(getManager(), weldClass);
//                }
//                event.fire();
//                // process the result
//                if (event.isVeto()) {
//                    getEnvironment().vetoClass(weldClass);
//                } else {
//                    boolean dirty = event.isDirty();
//                    if (dirty) {
//                        getEnvironment().removeClass(weldClass); // remove the original class
//                        AnnotatedType<?> modifiedType;
//                        if (synthetic) {
//                            modifiedType = ExternalAnnotatedType.of(event.getAnnotatedType());
//                        } else {
//                            modifiedType = DiscoveredExternalAnnotatedType.of(event.getAnnotatedType(), weldClass);
//                        }
//                        weldClass = classTransformer.loadClass(modifiedType);
//                    }
//
//                    // vetoed due to @Veto or @Requires
//                    boolean vetoed = Beans.isVetoed(weldClass);
//
//                    if (dirty && !vetoed) {
//                        getEnvironment().addClass(weldClass); // add a replacement for the removed class
//                    }
//                    if (!dirty && vetoed) {
//                        getEnvironment().vetoClass(weldClass);
//                    }
//                }
//            }
//        });
//    }
//
//    @Override
//    protected void processBeanAttributes(Collection<? extends AbstractBean<?, ?>> beans) {
//        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<AbstractBean<?, ?>>(beans) {
//            protected void doWork(AbstractBean<?, ?> bean) {
//                // fire ProcessBeanAttributes for class beans
//                boolean vetoed = fireProcessBeanAttributes(bean);
//                if (vetoed) {
//                    if (bean.isSpecializing()) {
//                        BeansClosure.getClosure(getManager()).removeSpecialized(bean.getSpecializedBean());
//                        getQueue().add(bean.getSpecializedBean());
//                    }
//                    getEnvironment().vetoBean(bean);
//                } else {
//                    // now that we know that the bean won't be vetoed, it's the right time to register @New injection points
//                    getEnvironment().addNewBeansFromInjectionPoints(bean);
//                }
//            }
//        });
//    }
//
//    @Override
//    public void createClassBeans() {
//        final Multimap<Class<?>, WeldClass<?>> otherWeldClasses = Multimaps.newSetMultimap(new ConcurrentHashMap<Class<?>, Collection<WeldClass<?>>>(),
//                new ConcurrentHashSetSupplier<WeldClass<?>>());
//
//        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<WeldClass<?>>(getEnvironment().getClasses()) {
//            protected void doWork(WeldClass<?> weldClass) {
//                createClassBean(weldClass, otherWeldClasses);
//            }
//        });
//
//        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<InternalEjbDescriptor<?>>(getEnvironment().getEjbDescriptors()) {
//            protected void doWork(InternalEjbDescriptor<?> descriptor) {
//                if (!getEnvironment().isVetoed(descriptor.getBeanClass())) {
//                    if (descriptor.isSingleton() || descriptor.isStateful() || descriptor.isStateless()) {
//                        if (otherWeldClasses.containsKey(descriptor.getBeanClass())) {
//                            for (WeldClass<?> c : otherWeldClasses.get(descriptor.getBeanClass())) {
//                                createSessionBean(descriptor, Reflections.<WeldClass> cast(c));
//                            }
//                        } else {
//                            createSessionBean(descriptor);
//                        }
//                    }
//                }
//            }
//        });
//    }
//
//    @Override
//    public void createProducersAndObservers() {
//        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<AbstractClassBean<?>>(getEnvironment().getClassBeanMap().values()) {
//            protected void doWork(AbstractClassBean<?> bean) {
//                createObserversProducersDisposers(bean);
//            }
//        });
//    }
//
//    @Override
//    public void doAfterBeanDiscovery(List<? extends Bean<?>> beanList) {
//        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<Bean<?>>(beanList) {
//            protected void doWork(Bean<?> bean) {
//                if (bean instanceof RIBean<?>) {
//                    ((RIBean<?>) bean).initializeAfterBeanDiscovery();
//                }
//            }
//        });
//    }
//
//    @Override
//    public AbstractBeanDeployer<BeanDeployerEnvironment> initializeBeans() {
//        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<RIBean<?>>(getEnvironment().getBeans()) {
//            protected void doWork(RIBean<?> bean) {
//                bean.initialize(getEnvironment());
//            }
//        });
//        return this;
//    }
//
//    @Override
//    public AbstractBeanDeployer<BeanDeployerEnvironment> fireBeanEvents() {
//        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<RIBean<?>>(getEnvironment().getBeans()) {
//            protected void doWork(RIBean<?> bean) {
//                fireBeanEvents(bean);
//            }
//        });
//        return this;
//    }
}
