package org.jboss.weld.test.unit.deployment.structure.nonTransitiveResolution;

import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

public class Foo
{
   
   // Inject the bean manager to make sure the bean is intranstive  
   @Inject private BeanManager beanManager;
   
   // Inject the @New bean to make sure the bean is intranstive
   @Inject @New String str;
   
   public BeanManager getBeanManager()
   {
      return beanManager;
   }
   
}
