package org.jboss.weld.tests.enterprise;

import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.ejb3.annotation.CacheConfig;

@Stateful
@SessionScoped
@CacheConfig(idleTimeoutSeconds=1)
public class HelloBean implements IHelloBean
{
   @Resource(mappedName = "java:app/BeanManager")
   private BeanManager beanManager;

   public String sayHello()
   {
      return "hello";
   }

   public String sayGoodbye()
   {
      return beanManager.getELResolver() != null ? "goodbye" : "error";
   }

   @Remove
   public void remove()
   {
   }
}
