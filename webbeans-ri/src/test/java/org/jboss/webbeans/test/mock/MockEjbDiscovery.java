package org.jboss.webbeans.test.mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.MessageDriven;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.jboss.webbeans.bootstrap.spi.EjbDiscovery;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;

public class MockEjbDiscovery implements EjbDiscovery
{
   
   private final List<EjbDescriptor<?>> ejbs;

   public MockEjbDiscovery(Iterable<Class<?>> allClasses)
   {
      
      this.ejbs = new ArrayList<EjbDescriptor<?>>();
      for (Class<?> ejbClass : discoverEjbs(allClasses))
      {
         this.ejbs.add(MockEjbDescriptor.of(ejbClass));
      }
   }

   public Iterable<EjbDescriptor<?>> discoverEjbs()
   {
      return ejbs;
   }
   
   protected static Iterable<Class<?>> discoverEjbs(Iterable<Class<?>> webBeanClasses)
   {
      Set<Class<?>> ejbs = new HashSet<Class<?>>();
      for (Class<?> clazz : webBeanClasses)
      {
         if (clazz.isAnnotationPresent(Stateless.class) || clazz.isAnnotationPresent(Stateful.class) || clazz.isAnnotationPresent(MessageDriven.class)) 
         {
            ejbs.add(clazz);
         }
      }
      return ejbs;
   }
   
}
