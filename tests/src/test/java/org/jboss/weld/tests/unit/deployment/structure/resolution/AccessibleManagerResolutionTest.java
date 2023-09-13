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
package org.jboss.weld.tests.unit.deployment.structure.resolution;

import static org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider.STATIC_INSTANCE;

import java.util.Collections;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.attributes.BeanAttributesFactory;
import org.jboss.weld.bean.proxy.DefaultProxyInstantiator;
import org.jboss.weld.bean.proxy.ProxyInstantiator;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.BeanDeployerEnvironmentFactory;
import org.jboss.weld.bootstrap.SpecializationAndEnablementRegistry;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.event.DefaultObserverNotifierFactory;
import org.jboss.weld.event.GlobalObserverNotifierService;
import org.jboss.weld.injection.ResourceInjectionFactory;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.interceptor.builder.InterceptorsApiAbstraction;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.module.EjbSupport;
import org.jboss.weld.module.ExpressionLanguageSupport;
import org.jboss.weld.module.ObserverNotifierFactory;
import org.jboss.weld.module.web.WeldWebModule;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.ReflectionCacheFactory;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.security.NoopSecurityServices;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.serialization.BeanIdentifierIndex;
import org.jboss.weld.serialization.ContextualStoreImpl;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AccessibleManagerResolutionTest {

    private ClassTransformer classTransformer;
    private TypeStore typeStore;
    private ServiceRegistry services;
    private BeanManagerImpl root;

    @BeforeMethod
    public void beforeMethod() {
        BeanIdentifierIndex beanIdentifierIndex = new BeanIdentifierIndex();
        beanIdentifierIndex.build(Collections.<Bean<?>> emptySet());
        this.typeStore = new TypeStore();
        this.classTransformer = new ClassTransformer(typeStore, new SharedObjectCache(),
                ReflectionCacheFactory.newInstance(typeStore), RegistrySingletonProvider.STATIC_INSTANCE);
        this.services = new SimpleServiceRegistry();

        this.services.add(MetaAnnotationStore.class, new MetaAnnotationStore(classTransformer));
        this.services.add(ContextualStore.class, new ContextualStoreImpl(STATIC_INSTANCE, beanIdentifierIndex));
        this.services.add(ClassTransformer.class, classTransformer);
        this.services.add(SharedObjectCache.class, new SharedObjectCache());
        this.services.add(WeldConfiguration.class, new WeldConfiguration(this.services, new MockDeployment(services)));
        this.services.add(SecurityServices.class, NoopSecurityServices.INSTANCE);
        this.services.add(ObserverNotifierFactory.class, DefaultObserverNotifierFactory.INSTANCE);
        this.services.add(GlobalObserverNotifierService.class,
                new GlobalObserverNotifierService(services, RegistrySingletonProvider.STATIC_INSTANCE));
        this.services.add(ExpressionLanguageSupport.class, WeldWebModule.EL_SUPPORT);
        this.services.add(SpecializationAndEnablementRegistry.class, new SpecializationAndEnablementRegistry());
        this.services.add(InterceptorsApiAbstraction.class, new InterceptorsApiAbstraction(DefaultResourceLoader.INSTANCE));
        this.services.add(ProxyInstantiator.class, DefaultProxyInstantiator.INSTANCE);
        this.services.add(ResourceInjectionFactory.class, new ResourceInjectionFactory());
        this.services.add(EjbSupport.class, EjbSupport.NOOP_IMPLEMENTATION);

        // create BeanManagerImpl and initialize container
        root = BeanManagerImpl.newRootManager(STATIC_INSTANCE, "root", services);
        Container.initialize(root, services);

        // add injection target service; has to be done once container was initialized
        this.services.add(InjectionTargetService.class, new InjectionTargetService(root));
    }

    @AfterMethod
    public void cleanup() {
        if (root != null) {
            Container.instance(root).cleanup();
        }
    }

    private <T> void addBean(BeanManagerImpl manager, Class<T> c) {
        EnhancedAnnotatedType<T> clazz = manager.createEnhancedAnnotatedType(c);
        RIBean<?> bean = ManagedBean.of(BeanAttributesFactory.forBean(clazz, manager), clazz, manager);
        manager.addBean(bean);
        manager.getBeanResolver().clear();
        BeanDeployerEnvironment environment = BeanDeployerEnvironmentFactory.newEnvironment(manager);
        bean.initialize(environment);
    }

    @Test
    public void testAccessibleDynamicallySingleLevel() {
        BeanManagerImpl child = BeanManagerImpl.newRootManager(STATIC_INSTANCE, "child", services);
        addBean(root, Cow.class);
        Assert.assertEquals(1, root.getBeans(Cow.class).size());
        Assert.assertEquals(0, child.getBeans(Cow.class).size());
        child.addAccessibleBeanManager(root);
        Assert.assertEquals(1, child.getBeans(Cow.class).size());
        addBean(child, Chicken.class);
        Assert.assertEquals(1, child.getBeans(Chicken.class).size());
        Assert.assertEquals(0, root.getBeans(Chicken.class).size());
    }

    @Test
    public void testAccessibleThreeLevelsWithMultiple() {
        BeanManagerImpl child = BeanManagerImpl.newRootManager(STATIC_INSTANCE, "child", services);
        BeanManagerImpl child1 = BeanManagerImpl.newRootManager(STATIC_INSTANCE, "child1", services);
        BeanManagerImpl grandchild = BeanManagerImpl.newRootManager(STATIC_INSTANCE, "grandchild", services);
        BeanManagerImpl greatGrandchild = BeanManagerImpl.newRootManager(STATIC_INSTANCE, "greatGrandchild", services);
        child.addAccessibleBeanManager(root);
        grandchild.addAccessibleBeanManager(child1);
        grandchild.addAccessibleBeanManager(child);
        addBean(greatGrandchild, Cat.class);
        greatGrandchild.addAccessibleBeanManager(grandchild);
        addBean(root, Cow.class);
        addBean(child, Chicken.class);
        addBean(grandchild, Pig.class);
        addBean(child1, Horse.class);
        Assert.assertEquals(0, root.getBeans(Pig.class).size());
        Assert.assertEquals(0, root.getBeans(Chicken.class).size());
        Assert.assertEquals(1, root.getBeans(Cow.class).size());
        Assert.assertEquals(0, root.getBeans(Horse.class).size());
        Assert.assertEquals(0, root.getBeans(Cat.class).size());
        Assert.assertEquals(0, child.getBeans(Pig.class).size());
        Assert.assertEquals(1, child.getBeans(Chicken.class).size());
        Assert.assertEquals(1, child.getBeans(Cow.class).size());
        Assert.assertEquals(0, child.getBeans(Horse.class).size());
        Assert.assertEquals(0, child.getBeans(Cat.class).size());
        Assert.assertEquals(0, child1.getBeans(Cow.class).size());
        Assert.assertEquals(1, child1.getBeans(Horse.class).size());
        Assert.assertEquals(1, grandchild.getBeans(Pig.class).size());
        Assert.assertEquals(1, grandchild.getBeans(Chicken.class).size());
        Assert.assertEquals(0, grandchild.getBeans(Cow.class).size());
        Assert.assertEquals(1, grandchild.getBeans(Horse.class).size());
        Assert.assertEquals(0, grandchild.getBeans(Cat.class).size());
        Assert.assertEquals(1, greatGrandchild.getBeans(Pig.class).size());
        Assert.assertEquals(0, greatGrandchild.getBeans(Chicken.class).size());
        Assert.assertEquals(0, greatGrandchild.getBeans(Cow.class).size());
        Assert.assertEquals(0, greatGrandchild.getBeans(Horse.class).size());
        Assert.assertEquals(1, greatGrandchild.getBeans(Cat.class).size());
    }

    @Test
    public void testSameManagerAddedTwice() {
        BeanManagerImpl child = BeanManagerImpl.newRootManager(STATIC_INSTANCE, "child", services);
        BeanManagerImpl grandchild = BeanManagerImpl.newRootManager(STATIC_INSTANCE, "grandchild", services);
        grandchild.addAccessibleBeanManager(child);
        child.addAccessibleBeanManager(root);
        grandchild.addAccessibleBeanManager(root);
        addBean(root, Cow.class);
        addBean(child, Chicken.class);
        addBean(grandchild, Pig.class);
        Assert.assertEquals(0, root.getBeans(Pig.class).size());
        Assert.assertEquals(0, root.getBeans(Chicken.class).size());
        Assert.assertEquals(1, root.getBeans(Cow.class).size());
        Assert.assertEquals(0, child.getBeans(Pig.class).size());
        Assert.assertEquals(1, child.getBeans(Chicken.class).size());
        Assert.assertEquals(1, child.getBeans(Cow.class).size());
        Assert.assertEquals(1, grandchild.getBeans(Pig.class).size());
        Assert.assertEquals(1, grandchild.getBeans(Chicken.class).size());
        Assert.assertEquals(1, grandchild.getBeans(Cow.class).size());
    }

    @Test
    public void testCircular() {
        BeanManagerImpl child = BeanManagerImpl.newRootManager(STATIC_INSTANCE, "child", services);
        BeanManagerImpl grandchild = BeanManagerImpl.newRootManager(STATIC_INSTANCE, "grandchild", services);
        grandchild.addAccessibleBeanManager(child);
        child.addAccessibleBeanManager(root);
        grandchild.addAccessibleBeanManager(root);
        root.addAccessibleBeanManager(grandchild);
        addBean(root, Cow.class);
        addBean(child, Chicken.class);
        addBean(grandchild, Pig.class);
        Assert.assertEquals(1, root.getBeans(Pig.class).size());
        Assert.assertEquals(0, root.getBeans(Chicken.class).size());
        Assert.assertEquals(1, root.getBeans(Cow.class).size());
        Assert.assertEquals(0, child.getBeans(Pig.class).size());
        Assert.assertEquals(1, child.getBeans(Chicken.class).size());
        Assert.assertEquals(1, child.getBeans(Cow.class).size());
        Assert.assertEquals(1, grandchild.getBeans(Pig.class).size());
        Assert.assertEquals(1, grandchild.getBeans(Chicken.class).size());
        Assert.assertEquals(1, grandchild.getBeans(Cow.class).size());
    }

}
