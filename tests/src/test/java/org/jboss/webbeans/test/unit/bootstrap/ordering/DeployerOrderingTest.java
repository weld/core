package org.jboss.webbeans.test.unit.bootstrap.ordering;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.AnnotationLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
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
import org.jboss.webbeans.mock.MockEjbDescriptor;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

import com.acme.RoadRunner;

@Artifact
@Packaging(PackagingType.EAR)
@Classes(packages="com.acme")
public class DeployerOrderingTest extends AbstractWebBeansTest
{
   
   @Test(groups="bootstrap")
   public void testNewSimpleBeansAfterNonNew()
   {
      BeanDeployer beanDeployer = new BeanDeployer(manager);
      beanDeployer.addClasses(Arrays.asList(Cow.class, Tuna.class));
      beanDeployer.createBeans();
      assert beanDeployer.getBeans().size() == 4;
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beanDeployer.getBeans()).subList(0, 2))
      {
         assert !(bean instanceof NewBean);
         assert bean instanceof SimpleBean;
      }
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beanDeployer.getBeans()).subList(2, 4))
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
      assert beanDeployer.getBeans().size() == 4;
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beanDeployer.getBeans()).subList(0, 2))
      {
         assert !(bean instanceof NewBean);
         assert bean instanceof EnterpriseBean;
      }
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beanDeployer.getBeans()).subList(2, 4))
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
      assert beanDeployer.getBeans().size() == 8;
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beanDeployer.getBeans()).subList(0, 4))
      {
         assert bean instanceof AbstractStandardBean;
      }
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beanDeployer.getBeans()).subList(4, 6))
      {
         assert !(bean instanceof NewBean);
         assert bean instanceof SimpleBean;
      }
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beanDeployer.getBeans()).subList(6, 8))
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
      assert beanDeployer.getBeans().size() == 6;
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beanDeployer.getBeans()).subList(0, 4))
      {
         assert bean.getType().getName().startsWith("org.jboss.webbeans");
      }
      for (RIBean<?> bean : new ArrayList<RIBean<?>>(beanDeployer.getBeans()).subList(4, 6))
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
      assert beanDeployer.getBeans().size() == 5;
      for (RIBean<?> bean : beanDeployer.getBeans())
      {
         if (bean.getType().equals(TarantulaProducer.class))
         {
            indexOfProducerDeclaringBean = i; 
         }
         if (bean.getType().equals(Tarantula.class) && bean.getBindings().contains(new AnnotationLiteral<Tame>() {}))
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
      assert beanDeployer.getBeans().size() == 8;
      int indexOfSpider = 0;
      int indexOfTarantula = 0;
      int indexOfDefangedTarantula = 0;
      int i = 0;
      for (RIBean<?> bean : beanDeployer.getBeans())
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

}
