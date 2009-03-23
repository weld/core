package org.jboss.webbeans.test.unit.bootstrap.ordering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.AnnotationLiteral;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.NewBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.bean.standard.AbstractStandardBean;
import org.jboss.webbeans.bean.standard.EventBean;
import org.jboss.webbeans.bean.standard.InjectionPointBean;
import org.jboss.webbeans.bean.standard.InstanceBean;
import org.jboss.webbeans.bean.standard.ManagerBean;
import org.jboss.webbeans.bootstrap.BeanDeployer;
import org.jboss.webbeans.bootstrap.BootstrapOrderingBeanComparator;
import org.jboss.webbeans.literal.NewLiteral;
import org.jboss.webbeans.mock.MockEjbDescriptor;
import org.jboss.webbeans.mock.MockServletLifecycle;
import org.jboss.webbeans.mock.MockWebBeanDiscovery;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.acme.RoadRunner;

public class DeployerOrderingTest
{
   
   private MockServletLifecycle lifecycle;
   private ManagerImpl manager;
   
   @BeforeClass
   public void beforeClass() throws Throwable
   {
      lifecycle = new MockServletLifecycle(); 
      lifecycle.initialize();
      MockWebBeanDiscovery discovery = lifecycle.getWebBeanDiscovery();
      lifecycle.beginApplication();
      lifecycle.beginSession();
      lifecycle.beginRequest();
      manager = CurrentManager.rootManager();
   }
   
   @AfterClass(alwaysRun=true)
   public void afterClass() throws Exception
   {
      lifecycle.endRequest();
      lifecycle.endSession();
      lifecycle.endApplication();
      CurrentManager.setRootManager(null);
      lifecycle = null;
   }
   
   @Test(groups="bootstrap")
   public void testNewSimpleBeansAfterNonNew()
   {
      BeanDeployer beanDeployer = new BeanDeployer(manager);
      beanDeployer.addClasses(Arrays.asList(Cow.class, Tuna.class));
      beanDeployer.createBeans();
      Set<RIBean<?>> beans = new TreeSet<RIBean<?>>(new BootstrapOrderingBeanComparator());
      beans.addAll(beanDeployer.getBeanDeployerEnvironment().getBeans());
      assert beans.size() == 4;
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beans).subList(0, 2))
      {
         assert !(bean instanceof NewBean);
         assert bean instanceof SimpleBean;
      }
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beans).subList(2, 4))
      {
         assert (bean instanceof NewBean);
         assert bean instanceof SimpleBean;
      }
   }
   
   @Test(groups="bootstrap")
   public void testNewEnterpriseBeansAfterNonNew()
   {
      BeanDeployer beanDeployer = new BeanDeployer(manager);
      beanDeployer.addClasses(Arrays.asList(Lion.class, Gazelle.class));
      manager.getEjbDescriptorCache().add(MockEjbDescriptor.of(Lion.class));
      manager.getEjbDescriptorCache().add(MockEjbDescriptor.of(Gazelle.class));
      beanDeployer.createBeans();
      Set<RIBean<?>> beans = new TreeSet<RIBean<?>>(new BootstrapOrderingBeanComparator());
      beans.addAll(beanDeployer.getBeanDeployerEnvironment().getBeans());
      assert beans.size() == 4;
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beans).subList(0, 2))
      {
         assert !(bean instanceof NewBean);
         assert bean instanceof EnterpriseBean;
      }
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beans).subList(2, 4))
      {
         assert (bean instanceof NewBean);
         assert bean instanceof EnterpriseBean;
      }
   }
   
   @Test(groups="bootstrap")
   public void testStandardBeansBeforeAll()
   {
      BeanDeployer beanDeployer = new BeanDeployer(manager);
      beanDeployer.addBean(EventBean.of(manager)).addBean(InjectionPointBean.of(manager)).addBean(InstanceBean.of(manager)).addBean(ManagerBean.of(manager));
      beanDeployer.addClasses(Arrays.asList(Cow.class, Tuna.class));
      beanDeployer.createBeans();
      Set<RIBean<?>> beans = new TreeSet<RIBean<?>>(new BootstrapOrderingBeanComparator());
      beans.addAll(beanDeployer.getBeanDeployerEnvironment().getBeans());
      assert beans.size() == 8;
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beans).subList(0, 4))
      {
         assert bean instanceof AbstractStandardBean;
      }
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beans).subList(4, 6))
      {
         assert !(bean instanceof NewBean);
         assert bean instanceof SimpleBean;
      }
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beans).subList(6, 8))
      {
         assert (bean instanceof NewBean);
         assert bean instanceof SimpleBean;
      }
   }
   
   @Test(groups="bootstrap")
   public void testOrgJbossWebbeansBeforeUsers()
   {
      BeanDeployer beanDeployer = new BeanDeployer(manager);
      beanDeployer.addClasses(Arrays.asList(Cow.class, Tuna.class, RoadRunner.class));
      beanDeployer.createBeans();
      Set<RIBean<?>> beans = new TreeSet<RIBean<?>>(new BootstrapOrderingBeanComparator());
      beans.addAll(beanDeployer.getBeanDeployerEnvironment().getBeans());
      assert beans.size() == 6;
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beans).subList(0, 4))
      {
         assert bean.getType().getName().startsWith("org.jboss.webbeans");
      }
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beans).subList(4, 6))
      {
         assert bean.getType().getName().startsWith("com.acme");
      }
   }
   
   @Test(groups="bootstrap")
   public void testProducerMethodAfterDeclaringBean()
   {
      BeanDeployer beanDeployer = new BeanDeployer(manager);
      beanDeployer.addClasses(Arrays.asList(TarantulaProducer.class, Tuna.class));
      beanDeployer.createBeans();
      int indexOfProducerDeclaringBean = 0;
      int indexOfProducer = 0;
      int i = 0;
      Set<RIBean<?>> beans = new TreeSet<RIBean<?>>(new BootstrapOrderingBeanComparator());
      beans.addAll(beanDeployer.getBeanDeployerEnvironment().getBeans());
      assert beans.size() == 5;
      for (RIBean<?> bean : beans)
      {
         if (bean.getType().equals(TarantulaProducer.class) && !bean.getBindings().contains(new NewLiteral()))
         {
            indexOfProducerDeclaringBean = i; 
         }
         if (bean.getType().equals(Tarantula.class) && !bean.getBindings().contains(new NewLiteral()))
         {
            indexOfProducer = i;
         }
         i++;
      }
      assert indexOfProducer > indexOfProducerDeclaringBean;
   }
      
   @Test(groups="bootstrap")
   public void testClassHierarchies()
   {
      BeanDeployer beanDeployer = new BeanDeployer(manager);
      beanDeployer.addClasses(Arrays.asList(Spider.class, Tarantula.class, DefangedTarantula.class, Tuna.class));
      beanDeployer.createBeans();
      Set<RIBean<?>> beans = new TreeSet<RIBean<?>>(new BootstrapOrderingBeanComparator());
      beans.addAll(beanDeployer.getBeanDeployerEnvironment().getBeans());
      assert beans.size() == 8;
      int indexOfSpider = 0;
      int indexOfTarantula = 0;
      int indexOfDefangedTarantula = 0;
      int i = 0;
      for (RIBean<?> bean : beans)
      {
         if (bean.getType().equals(Spider.class))
         {
            indexOfSpider = i; 
         }
         if (bean.getType().equals(Tarantula.class))
         {
            indexOfTarantula = i;
         }
         if (bean.getType().equals(DefangedTarantula.class))
         {
            indexOfDefangedTarantula = i;
         }
         i++;
      }
      assert indexOfDefangedTarantula > indexOfTarantula;
      assert indexOfTarantula > indexOfSpider;
   }
   
   @Test(groups="bootstrap")
   public void testClassHierarchiesForMethods()
   {
      BeanDeployer beanDeployer = new BeanDeployer(manager);
      beanDeployer.addClasses(Arrays.asList(Shop.class, JewelryShop.class, Tuna.class));
      beanDeployer.createBeans();
      Set<RIBean<?>> beans = new TreeSet<RIBean<?>>(new BootstrapOrderingBeanComparator());
      beans.addAll(beanDeployer.getBeanDeployerEnvironment().getBeans());
      assert beans.size() == 8;
      int indexOfShop = 0;
      int indexOfJewelryShop = 0;
      int indexOfExpensiveGift = 0;
      int indexOfNecklace = 0;
      int i = 0;
      for (RIBean<?> bean : beans)
      {
         if (bean.getType().equals(Shop.class) && !bean.getBindings().contains(new NewLiteral()))
         {
            indexOfShop = i;
         }
         if (bean.getType().equals(JewelryShop.class) && !bean.getBindings().contains(new NewLiteral()))
         {
            indexOfJewelryShop = i;
         }
         if (bean.getType().equals(Product.class) && bean.getBindings().contains(new AnnotationLiteral<Sparkly>() {}))
         {
            indexOfNecklace = i;
         }
         if (bean.getType().equals(Product.class) && !bean.getBindings().contains(new AnnotationLiteral<Sparkly>() {}))
         {
            indexOfExpensiveGift = i;
         }
         i++;
      }
      assert indexOfJewelryShop > indexOfShop;
      assert indexOfExpensiveGift > indexOfShop;
      assert indexOfNecklace > indexOfJewelryShop;
      assert indexOfNecklace > indexOfExpensiveGift;
   }

}
