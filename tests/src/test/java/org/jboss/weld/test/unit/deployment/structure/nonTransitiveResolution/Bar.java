package org.jboss.weld.test.unit.deployment.structure.nonTransitiveResolution;

import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

public class Bar
{
   
   @Inject private Foo foo;
   
   // Inject the bean manager to make sure the bean is intranstive
   @Inject private BeanManager beanManager;
   
   // Inject the @New bean to make sure the bean is intranstive   
   @Inject @New String str;
   
   public Foo getFoo()
   {
      return foo;
   }
   
   public BeanManager getBeanManager()
   {
      return beanManager;
   }

}
