package org.jboss.weld.tests.beanManager;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BeanManagerTest
{
   @Deployment
   public static Archive<?> deploy() 
   {
      return ShrinkWrap.create(BeanArchive.class)
         .addPackage(BeanManagerTest.class.getPackage())
         .addClass(Utils.class);
   }

   @Inject
   private BeanManagerImpl beanManager;

   @Test(expected=IllegalArgumentException.class)
   public void testNullBeanArgumentToGetReference()
   {
      Bean<Foo> bean = Utils.getBean(beanManager, Foo.class);
      CreationalContext<Foo> cc = beanManager.createCreationalContext(bean);
      beanManager.getReference(null, Foo.class, cc);
   }
   
   @Test(expected=IllegalArgumentException.class)
   public void testNullBeanTypeArgumentToGetReference()
   {
      Bean<Foo> bean = Utils.getBean(beanManager, Foo.class);
      CreationalContext<Foo> cc = beanManager.createCreationalContext(bean);
      beanManager.getReference(bean, null, cc);
   }
   
   @Test(expected=IllegalArgumentException.class)
   public void testNullCreationalContextArgumentToGetReference()
   {
      Bean<Foo> bean = Utils.getBean(beanManager, Foo.class);
      beanManager.getReference(bean, Foo.class, null);
   }
}
