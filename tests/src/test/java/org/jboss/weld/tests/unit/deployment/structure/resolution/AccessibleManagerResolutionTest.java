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

import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.Container;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.interceptor.InterceptionMetadataService;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.ContextualStoreImpl;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.xml.EnabledClasses;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AccessibleManagerResolutionTest
{
   
   private ClassTransformer classTransformer;
   private ServiceRegistry services;
   
   @BeforeMethod
   public void beforeMethod()
   {
      this.classTransformer = new ClassTransformer(new TypeStore());
      this.services = new SimpleServiceRegistry();
      this.services.add(MetaAnnotationStore.class, new MetaAnnotationStore(classTransformer));
      this.services.add(ContextualStore.class, new ContextualStoreImpl());
      this.services.add(InterceptionMetadataService.class, new InterceptionMetadataService());
      this.services.add(ClassTransformer.class, classTransformer);
   }
   
   private void addBean(BeanManagerImpl manager, Class<?> c)
   {
      WeldClass<?> clazz = WeldClassImpl.of(c, classTransformer);
      RIBean<?> bean = ManagedBean.of(clazz, manager);
      manager.addBean(bean);
      BeanDeployerEnvironment environment = new BeanDeployerEnvironment(new EjbDescriptors(), manager);
      bean.initialize(environment);
   }
   
   @Test
   public void testAccessibleDynamicallySingleLevel()
   {
      BeanManagerImpl root = BeanManagerImpl.newRootManager("root", services, new EnabledClasses());
      Container.initialize(root, services);
      BeanManagerImpl child = BeanManagerImpl.newRootManager("child", services, new EnabledClasses());
      addBean(root, Cow.class);
      assert root.getBeans(Cow.class).size() == 1;
      assert child.getBeans(Cow.class).size() == 0;
      child.addAccessibleBeanManager(root);
      Set<Bean<?>> beans = child.getBeans(Cow.class);
      assert child.getBeans(Cow.class).size() == 1;
      addBean(child, Chicken.class);
      assert child.getBeans(Chicken.class).size() == 1;
      assert root.getBeans(Chicken.class).size() == 0;
   }
   
   @Test
   public void testAccessibleThreeLevelsWithMultiple()
   {
      BeanManagerImpl root = BeanManagerImpl.newRootManager("root", services, new EnabledClasses());
      Container.initialize(root, services);
      BeanManagerImpl child = BeanManagerImpl.newRootManager("child", services, new EnabledClasses());
      BeanManagerImpl child1 = BeanManagerImpl.newRootManager("child1", services, new EnabledClasses());
      BeanManagerImpl grandchild = BeanManagerImpl.newRootManager("grandchild", services, new EnabledClasses());
      BeanManagerImpl greatGrandchild = BeanManagerImpl.newRootManager("greatGrandchild", services, new EnabledClasses());
      child.addAccessibleBeanManager(root);
      grandchild.addAccessibleBeanManager(child1);
      grandchild.addAccessibleBeanManager(child);
      addBean(greatGrandchild, Cat.class);
      greatGrandchild.addAccessibleBeanManager(grandchild);
      addBean(root, Cow.class);
      addBean(child, Chicken.class);
      addBean(grandchild, Pig.class);
      addBean(child1, Horse.class);
      assert root.getBeans(Pig.class).size() == 0;
      assert root.getBeans(Chicken.class).size() == 0;
      assert root.getBeans(Cow.class).size() == 1;
      assert root.getBeans(Horse.class).size() == 0;
      assert root.getBeans(Cat.class).size() == 0;
      assert child.getBeans(Pig.class).size() == 0;
      assert child.getBeans(Chicken.class).size() == 1;
      assert child.getBeans(Cow.class).size() == 1;
      assert child.getBeans(Horse.class).size() == 0;
      assert child.getBeans(Cat.class).size() == 0;
      assert child1.getBeans(Cow.class).size() == 0;
      assert child1.getBeans(Horse.class).size() == 1;
      assert grandchild.getBeans(Pig.class).size() == 1;
      assert grandchild.getBeans(Chicken.class).size() == 1;
      assert grandchild.getBeans(Cow.class).size() == 1;
      assert grandchild.getBeans(Horse.class).size() ==1;
      assert grandchild.getBeans(Cat.class).size() == 0;
      assert greatGrandchild.getBeans(Pig.class).size() == 1;
      assert greatGrandchild.getBeans(Chicken.class).size() == 1;
      assert greatGrandchild.getBeans(Cow.class).size() == 1;
      assert greatGrandchild.getBeans(Horse.class).size() ==1;
      assert greatGrandchild.getBeans(Cat.class).size() == 1;
   }
   
   @Test
   public void testSameManagerAddedTwice()
   {
      BeanManagerImpl root = BeanManagerImpl.newRootManager("root", services, new EnabledClasses());
      Container.initialize(root, services);
      BeanManagerImpl child = BeanManagerImpl.newRootManager("child", services, new EnabledClasses());
      BeanManagerImpl grandchild = BeanManagerImpl.newRootManager("grandchild", services, new EnabledClasses());
      grandchild.addAccessibleBeanManager(child);
      child.addAccessibleBeanManager(root);
      grandchild.addAccessibleBeanManager(root);
      addBean(root, Cow.class);
      addBean(child, Chicken.class);
      addBean(grandchild, Pig.class);
      assert root.getBeans(Pig.class).size() == 0;
      assert root.getBeans(Chicken.class).size() == 0;
      assert root.getBeans(Cow.class).size() == 1;
      assert child.getBeans(Pig.class).size() == 0;
      assert child.getBeans(Chicken.class).size() == 1;
      assert child.getBeans(Cow.class).size() == 1;
      assert grandchild.getBeans(Pig.class).size() == 1;
      assert grandchild.getBeans(Chicken.class).size() == 1;
      assert grandchild.getBeans(Cow.class).size() == 1;
   }
   
   @Test
   public void testCircular()
   {
      BeanManagerImpl root = BeanManagerImpl.newRootManager("root", services, new EnabledClasses());
      Container.initialize(root, services);
      BeanManagerImpl child = BeanManagerImpl.newRootManager("child", services, new EnabledClasses());
      BeanManagerImpl grandchild = BeanManagerImpl.newRootManager("grandchild", services, new EnabledClasses());
      grandchild.addAccessibleBeanManager(child);
      child.addAccessibleBeanManager(root);
      grandchild.addAccessibleBeanManager(root);
      root.addAccessibleBeanManager(grandchild);
      addBean(root, Cow.class);
      addBean(child, Chicken.class);
      addBean(grandchild, Pig.class);
      assert root.getBeans(Pig.class).size() == 1;
      assert root.getBeans(Chicken.class).size() == 1;
      assert root.getBeans(Cow.class).size() == 1;
      assert child.getBeans(Pig.class).size() == 1;
      assert child.getBeans(Chicken.class).size() == 1;
      assert child.getBeans(Cow.class).size() == 1;
      assert grandchild.getBeans(Pig.class).size() == 1;
      assert grandchild.getBeans(Chicken.class).size() == 1;
      assert grandchild.getBeans(Cow.class).size() == 1;
   }
   
}
