package org.jboss.webbeans.tck;

import org.jboss.jsr299.tck.spi.Beans;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.util.Reflections;

/**
 * Implements the Beans SPI for the TCK specifically for the JBoss RI.
 * 
 * @author Shane Bryzak
 * @author Pete Muir
 * @author David Allen
 * 
 */
public class BeansImpl implements Beans
{

   public boolean isEnterpriseBean(Class<?> clazz)
   {
      return CurrentManager.rootManager().getEjbDescriptorCache().containsKey(clazz);
   }

   public boolean isEntityBean(Class<?> clazz)
   {
      if (CurrentManager.rootManager().getEjbDescriptorCache().containsKey(clazz))
      {
         for (EjbDescriptor<?> ejbDescriptor : CurrentManager.rootManager().getEjbDescriptorCache().get(clazz))
         {
            if (!ejbDescriptor.isMessageDriven() && !ejbDescriptor.isSingleton() && !ejbDescriptor.isStateful() && !ejbDescriptor.isStateless())
            {
               return true;
            }
         }
      }
      return false;
   }

   public boolean isStatefulBean(Class<?> clazz)
   {
      if (CurrentManager.rootManager().getEjbDescriptorCache().containsKey(clazz))
      {
         for (EjbDescriptor<?> ejbDescriptor : CurrentManager.rootManager().getEjbDescriptorCache().get(clazz))
         {
            if (ejbDescriptor.isStateful())
            {
               return true;
            }
         }
      }
      return false;
   }

   public boolean isStatelessBean(Class<?> clazz)
   {
      if (CurrentManager.rootManager().getEjbDescriptorCache().containsKey(clazz))
      {
         for (EjbDescriptor<?> ejbDescriptor : CurrentManager.rootManager().getEjbDescriptorCache().get(clazz))
         {
            if (ejbDescriptor.isStateless())
            {
               return true;
            }
         }
      }
      return false;
   }
   
   public boolean isProxy(Object instance)
   {
      return Reflections.isProxy(instance);
   }

   public <T> T getEnterpriseBean(Class<? extends T> beanType, Class<T> localInterface)
   {
      T enterpriseBean = null;
      if (CurrentManager.rootManager().getEjbDescriptorCache().containsKey(beanType))
      {
         EjbDescriptor<?> ejbDescriptor = CurrentManager.rootManager().getEjbDescriptorCache().get(beanType).iterator().next();
         String jndiName = null;
         for (BusinessInterfaceDescriptor<?> businessInterface : ejbDescriptor.getLocalBusinessInterfaces())
         {
            if (businessInterface.getInterface().equals(localInterface))
            {
               jndiName = businessInterface.getJndiName();
            }
         }
         if (jndiName == null)
            throw new NullPointerException("No JNDI name found for interface " + localInterface.getName() + " on bean " + beanType.getName());
         enterpriseBean = CurrentManager.rootManager().getNaming().lookup(jndiName, localInterface);
      }      
      return enterpriseBean;
   }

}
