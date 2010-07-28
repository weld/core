package org.jboss.weld.tests.beanManager;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class BeanManagerTest extends AbstractWeldTest
{
   
   @Test(expectedExceptions=IllegalArgumentException.class)
   public void testNullBeanArgumentToGetReference()
   {
      Bean<Foo> bean = getBean(Foo.class);
      CreationalContext<Foo> cc = getCurrentManager().createCreationalContext(bean);
      getCurrentManager().getReference(null, Foo.class, cc);
   }
   
   @Test(expectedExceptions=IllegalArgumentException.class)
   public void testNullBeanTypeArgumentToGetReference()
   {
      Bean<Foo> bean = getBean(Foo.class);
      CreationalContext<Foo> cc = getCurrentManager().createCreationalContext(bean);
      getCurrentManager().getReference(bean, null, cc);
   }
   
   @Test(expectedExceptions=IllegalArgumentException.class)
   public void testNullCreationalContextArgumentToGetReference()
   {
      Bean<Foo> bean = getBean(Foo.class);
      CreationalContext<Foo> cc = getCurrentManager().createCreationalContext(bean);
      getCurrentManager().getReference(bean, Foo.class, null);
   }

}
