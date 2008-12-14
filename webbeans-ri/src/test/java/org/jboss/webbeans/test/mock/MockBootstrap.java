package org.jboss.webbeans.test.mock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bootstrap.WebBeansBootstrap;

public class MockBootstrap extends WebBeansBootstrap
{ 
   
   public MockBootstrap(ManagerImpl manager)
   {
      super(manager);
   }
   
   /**
    * Discover any beans defined by the provided classes
    * 
    * Beans discovered are not registered with the manager
    * 
    * @param classes The classes to create Web Beans from
    * @return A set of Web Beans that represents the classes
    */
   public Set<AbstractBean<?, ?>> createBeans(Class<?>... classes)
   {
      return createBeans(new HashSet<Class<?>>(Arrays.asList(classes)));
   }
   
}
