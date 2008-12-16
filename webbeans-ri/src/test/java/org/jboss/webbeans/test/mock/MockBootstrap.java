package org.jboss.webbeans.test.mock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.ejb.EJB;
import org.jboss.webbeans.ejb.EjbDescriptorCache;

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
   @SuppressWarnings("unchecked")
   public Set<AbstractBean<?, ?>> createBeans(Class<?>... classes)
   {
      for (Class<?> clazz : classes) {
         if (EJB.isEjb(clazz)) {
            String ejbName = clazz.getSimpleName() + "/local";
            EjbDescriptorCache.instance().addEjbDescriptor(ejbName, new MockEjbDescriptor(ejbName, clazz));
         }
      }
      return createBeans(new HashSet<Class<?>>(Arrays.asList(classes)));
   }
   
}
