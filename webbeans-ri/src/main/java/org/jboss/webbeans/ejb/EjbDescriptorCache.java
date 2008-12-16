package org.jboss.webbeans.ejb;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jboss.webbeans.bootstrap.spi.EjbDescriptor;

public class EjbDescriptorCache
{
   private static EjbDescriptorCache instance;

   public static EjbDescriptorCache instance()
   {
      return instance;
   }

   static
   {
      instance = new EjbDescriptorCache();
   }

   private ConcurrentMap<String, EjbDescriptor<?>> ejbsByName;
   private ConcurrentMap<Class<?>, Set<EjbDescriptor<?>>> ejbsByBeanClass;

   public EjbDescriptorCache()
   {
      this.ejbsByName = new ConcurrentHashMap<String, EjbDescriptor<?>>();
      this.ejbsByBeanClass = new ConcurrentHashMap<Class<?>, Set<EjbDescriptor<?>>>();
   }

   public EjbDescriptor<?> get(String ejbName)
   {
      return ejbsByName.get(ejbName);
   }
   
   public Iterable<EjbDescriptor<?>> get(Class<?> beanClass)
   {
      return ejbsByBeanClass.get(beanClass);
   }

   public void add(EjbDescriptor<?> ejbDescriptor)
   {
      ejbsByName.put(ejbDescriptor.getEjbName(), ejbDescriptor);
      ejbsByBeanClass.putIfAbsent(ejbDescriptor.getType(), new CopyOnWriteArraySet<EjbDescriptor<?>>());
      ejbsByBeanClass.get(ejbDescriptor.getType()).add(ejbDescriptor);
   }
   
   public boolean containsKey(String ejbName)
   {
      return ejbsByName.containsKey(ejbName);
   }
   
   public boolean containsKey(Class<?> beanClass)
   {
      return ejbsByBeanClass.containsKey(beanClass);
   }
   
   public void addAll(Iterable<EjbDescriptor<?>> ejbDescriptors)
   {
      for (EjbDescriptor<?> ejbDescriptor : ejbDescriptors)
      {
         add(ejbDescriptor);
      }
   }

}
