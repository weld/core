package org.jboss.weld.test.unit.deployment.structure.resolution;

import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.Container;
import org.jboss.weld.ContextualStoreImpl;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.ContextualStore;
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
      BeanManagerImpl root = BeanManagerImpl.newRootManager("root", services);
      Container.initialize(root, services);
      BeanManagerImpl child = BeanManagerImpl.newRootManager("child", services);
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
      BeanManagerImpl root = BeanManagerImpl.newRootManager("root", services);
      Container.initialize(root, services);
      BeanManagerImpl child = BeanManagerImpl.newRootManager("child", services);
      BeanManagerImpl child1 = BeanManagerImpl.newRootManager("child1", services);
      BeanManagerImpl grandchild = BeanManagerImpl.newRootManager("grandchild", services);
      BeanManagerImpl greatGrandchild = BeanManagerImpl.newRootManager("greatGrandchild", services);
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
      BeanManagerImpl root = BeanManagerImpl.newRootManager("root", services);
      Container.initialize(root, services);
      BeanManagerImpl child = BeanManagerImpl.newRootManager("child", services);
      BeanManagerImpl grandchild = BeanManagerImpl.newRootManager("grandchild", services);
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
      BeanManagerImpl root = BeanManagerImpl.newRootManager("root", services);
      Container.initialize(root, services);
      BeanManagerImpl child = BeanManagerImpl.newRootManager("child", services);
      BeanManagerImpl grandchild = BeanManagerImpl.newRootManager("grandchild", services);
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
